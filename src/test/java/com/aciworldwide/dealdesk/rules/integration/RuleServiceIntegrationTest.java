package com.aciworldwide.dealdesk.rules.integration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.model.tcv.TCVCalculation;
import com.aciworldwide.dealdesk.repository.DealRepository;
import com.aciworldwide.dealdesk.rules.exception.RuleValidationException;
import com.aciworldwide.dealdesk.rules.model.DealRule;
import com.aciworldwide.dealdesk.rules.repository.RuleRepository;
import com.aciworldwide.dealdesk.rules.service.RuleService;

@SpringBootTest
@Testcontainers
@DisplayName("Rule Service Integration Tests")
class RuleServiceIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private RuleService ruleService;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private DealRepository dealRepository;

    @BeforeEach
    void setUp() {
        ruleRepository.deleteAll();
        dealRepository.deleteAll();
    }

    @Nested
    @DisplayName("Rule Creation Tests")
    class RuleCreationTests {
        @Test
        @DisplayName("Should create rule successfully")
        void createRule_Success() {
            // Given
            DealRule rule = createTestRule("Test Rule");

            // When
            DealRule savedRule = ruleService.createRule(rule);

            // Then
            assertThat(savedRule).isNotNull();
            assertThat(savedRule.getId()).isNotNull();
            assertThat(savedRule.getName()).isEqualTo("Test Rule");
        }

        @Test
        @DisplayName("Should fail when creating rule with invalid data")
        void createRule_Invalid() {
            // Given
            DealRule rule = new DealRule();

            // When/Then
            assertThrows(RuleValidationException.class, () -> ruleService.createRule(rule));
        }
    }

    @Nested
    @DisplayName("Rule Application Tests")
    class RuleApplicationTests {
        @Test
        @DisplayName("Should apply rules to deal successfully")
        void applyRules_Success() {
            // Given
            Deal deal = createTestDeal();
            DealRule rule = createTestRule("TCV Rule");
            ruleRepository.save(rule);

            // Verify the saved rule exists before evaluation
            assertThat(ruleRepository.findAll()).hasSize(1);

            // When
            Deal updatedDeal = ruleService.evaluateRules(deal);

            // Then
            assertThat(updatedDeal.getTcvCalculation()).isNotNull();
            assertThat(updatedDeal.getTcvCalculation().getFinalValue())
                .isGreaterThan(BigDecimal.ZERO);
        }    }

    private DealRule createTestRule(String name) {
        return DealRule.builder()
            .name(name)
            .description("Test rule description")
            .active(true)
            .required(true)
            .version(1)
            .category("TCV")
            .priority(1)
            .build();
    }

    private Deal createTestDeal() {
        return Deal.builder()
            .name("Test Deal")
            .status(DealStatus.DRAFT)
            .value(BigDecimal.valueOf(100000))
            .createdAt(ZonedDateTime.now())
            .tcvCalculation(TCVCalculation.builder()
                .baseValue(BigDecimal.valueOf(100000))
                .build())
            .build();
    }
}