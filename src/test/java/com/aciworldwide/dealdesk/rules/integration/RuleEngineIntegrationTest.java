package com.aciworldwide.dealdesk.rules.integration;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import org.jeasy.rules.api.Facts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.aciworldwide.dealdesk.integration.IntegrationTestBase;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.rules.model.RuleDefinition;
import com.aciworldwide.dealdesk.rules.service.RuleDefinitionService;
import com.aciworldwide.dealdesk.rules.service.RuleExecutionService;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Rule Engine Integration Tests")
public class RuleEngineIntegrationTest extends IntegrationTestBase {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private RuleDefinitionService ruleDefinitionService;

    @Autowired
    private RuleExecutionService ruleExecutionService;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Deal testDeal;
    private RuleDefinition discountRule;

    @BeforeEach
    void setUp() {
        cleanupDatabase();
        initializeTestData();
    }

    private void cleanupDatabase() {
        mongoTemplate.dropCollection(RuleDefinition.class);
    }

    private void initializeTestData() {
        // Initialize test deal
        testDeal = createTestDeal();
        
        // Initialize discount rule
        discountRule = createDiscountRule();
    }

    private Deal createTestDeal() {
        Deal deal = new Deal();
        deal.setValue(BigDecimal.valueOf(1000));
        return deal;
    }

    private RuleDefinition createDiscountRule() {
        return RuleDefinition.builder()
                .ruleKey("TEST_DISCOUNT_RULE")
                .name("Test Discount Rule")
                .description("Applies 10% discount to deals over $500")
                .category("PRICING")
                .priority(100)
                .conditionExpression("#deal.value > 500")
                .actionExpression("#deal.setValue(#deal.getValue().multiply(0.9))")
                .enabled(true)
                .parameters(new HashMap<>())
                .validFrom(LocalDateTime.now().minusDays(1))
                .createdBy("test-user")
                .createdAt(LocalDateTime.now())
                .lastModifiedBy("test-user")
                .lastModifiedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Basic Rule Execution Tests")
    class BasicRuleExecutionTests {
        
        @Test
        @DisplayName("Should successfully execute a basic rule")
        void fullRuleExecutionFlow_ShouldSucceed() {
            // Create rule
            RuleDefinition savedRule = ruleDefinitionService.createRule(discountRule);
            assertThat(savedRule.getId()).isNotNull();

            // Execute rules
            Facts facts = createDealFacts(testDeal);
            ruleExecutionService.executeRules("PRICING", facts);

            // Verify rule was applied
            assertThat(testDeal.getValue())
                    .isEqualByComparingTo(BigDecimal.valueOf(900)); // Original 1000 * 0.9
        }

        @ParameterizedTest
        @CsvSource({
            "1000, 900",  // Above threshold - should apply discount
            "400, 400"    // Below threshold - should not apply discount
        })
        @DisplayName("Should correctly apply rules based on conditions")
        void ruleExecution_ShouldApplyBasedOnConditions(BigDecimal initialValue, BigDecimal expectedValue) {
            testDeal.setValue(initialValue);
            ruleDefinitionService.createRule(discountRule);

            Facts facts = createDealFacts(testDeal);
            ruleExecutionService.executeRules("PRICING", facts);

            assertThat(testDeal.getValue()).isEqualByComparingTo(expectedValue);
        }
    }

    @Nested
    @DisplayName("Multiple Rules Execution Tests")
    class MultipleRulesExecutionTests {

        @Test
        @DisplayName("Should execute multiple rules in priority order")
        void ruleExecution_WithMultipleRules_ShouldExecuteInPriorityOrder() {
            // Create high priority rule
            RuleDefinition highPriorityRule = createHighPriorityRule();
            
            // Save both rules
            ruleDefinitionService.createRule(highPriorityRule);
            ruleDefinitionService.createRule(discountRule);

            // Execute rules
            Facts facts = createDealFacts(testDeal);
            ruleExecutionService.executeRules("PRICING", facts);

            // Verify rules were applied in correct order (1000 -> 1100 -> 990)
            assertThat(testDeal.getValue())
                    .isEqualByComparingTo(BigDecimal.valueOf(990));
        }

        @Test
        @DisplayName("Should handle concurrent rule modifications")
        void ruleExecution_WithConcurrentModifications_ShouldHandleGracefully() {
            List<RuleDefinition> rules = List.of(
                createHighPriorityRule(),
                discountRule,
                createAdditionalChargeRule()
            );
            
            rules.forEach(ruleDefinitionService::createRule);

            Facts facts = createDealFacts(testDeal);
            ruleExecutionService.executeRules("PRICING", facts);

            // Verify final value after all rules (1000 -> 1100 -> 990 -> 1090)
            assertThat(testDeal.getValue())
                    .isEqualByComparingTo(BigDecimal.valueOf(1089));
        }
    }

    @Nested
    @DisplayName("Rule State Tests")
    class RuleStateTests {
        
        @Test
        @DisplayName("Should skip disabled rules")
        void ruleExecution_WithDisabledRule_ShouldSkipRule() {
            discountRule.setEnabled(false);
            ruleDefinitionService.createRule(discountRule);

            Facts facts = createDealFacts(testDeal);
            ruleExecutionService.executeRules("PRICING", facts);

            assertThat(testDeal.getValue())
                    .isEqualByComparingTo(BigDecimal.valueOf(1000));
        }

        @Test
        @DisplayName("Should skip expired rules")
        void ruleExecution_WithExpiredRule_ShouldSkipRule() {
            discountRule.setValidTo(LocalDateTime.now().minusDays(1));
            ruleDefinitionService.createRule(discountRule);

            Facts facts = createDealFacts(testDeal);
            ruleExecutionService.executeRules("PRICING", facts);

            assertThat(testDeal.getValue())
                    .isEqualByComparingTo(BigDecimal.valueOf(1000));
        }

        @Test
        @DisplayName("Should handle future rules correctly")
        void ruleExecution_WithFutureRule_ShouldSkipRule() {
            discountRule.setValidFrom(LocalDateTime.now().plusDays(1));
            ruleDefinitionService.createRule(discountRule);

            Facts facts = createDealFacts(testDeal);
            ruleExecutionService.executeRules("PRICING", facts);

            assertThat(testDeal.getValue())
                    .isEqualByComparingTo(BigDecimal.valueOf(1000));
        }
    }

    private Facts createDealFacts(Deal deal) {
        Facts facts = new Facts();
        facts.put("deal", deal);
        return facts;
    }

    private RuleDefinition createHighPriorityRule() {
        return RuleDefinition.builder()
                .ruleKey("HIGH_PRIORITY_RULE")
                .name("High Priority Rule")
                .description("Adds 100 to deal value")
                .category("PRICING")
                .priority(200)
                .conditionExpression("true")
                .actionExpression("#deal.setValue(#deal.getValue().add(100))")
                .enabled(true)
                .parameters(new HashMap<>())
                .validFrom(LocalDateTime.now().minusDays(1))
                .createdBy("test-user")
                .createdAt(LocalDateTime.now())
                .lastModifiedBy("test-user")
                .lastModifiedAt(LocalDateTime.now())
                .build();
    }

    private RuleDefinition createAdditionalChargeRule() {
        return RuleDefinition.builder()
                .ruleKey("ADDITIONAL_CHARGE_RULE")
                .name("Additional Charge Rule")
                .description("Adds 10% processing fee")
                .category("PRICING")
                .priority(50)
                .conditionExpression("true")
                .actionExpression("#deal.setValue(#deal.getValue().multiply(1.1))")
                .enabled(true)
                .parameters(new HashMap<>())
                .validFrom(LocalDateTime.now().minusDays(1))
                .createdBy("test-user")
                .createdAt(LocalDateTime.now())
                .lastModifiedBy("test-user")
                .lastModifiedAt(LocalDateTime.now())
                .build();
    }
}