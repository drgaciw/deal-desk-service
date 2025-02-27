package com.aciworldwide.dealdesk.rules.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aciworldwide.dealdesk.rules.exception.RuleNotFoundException;
import com.aciworldwide.dealdesk.rules.exception.RuleValidationException;
import com.aciworldwide.dealdesk.rules.model.DealRule;

@ExtendWith(MockitoExtension.class)
@DisplayName("Rule Service Tests")
class RuleServiceTest {

    @Mock
    private RuleCachingService ruleCachingService;

    @Mock
    private RuleVersioningService versioningService;

    @InjectMocks
    private RuleService ruleService;

    @Nested
    @DisplayName("Get Rule Tests")
    class GetRuleTests {
        @Test
        @DisplayName("Should get rule by ID when exists")
        void shouldGetRuleById() {
            String ruleId = "test-rule";
            DealRule expectedRule = DealRule.builder()
                .id(ruleId)
                .name("Test Rule")
                .build();
            when(ruleCachingService.getRule(ruleId)).thenReturn(expectedRule);

            DealRule result = ruleService.getRuleById(ruleId);

            assertThat(result)
                .isNotNull()
                .isEqualTo(expectedRule);
        }

        @Test
        @DisplayName("Should throw exception when rule ID is empty")
        void shouldThrowExceptionWhenRuleIdEmpty() {
            assertThatThrownBy(() -> ruleService.getRuleById(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rule ID cannot be empty");
        }

        @Test
        @DisplayName("Should throw exception when rule not found")
        void shouldThrowExceptionWhenRuleNotFound() {
            String ruleId = "nonexistent";
            when(ruleCachingService.getRule(ruleId)).thenReturn(null);

            assertThatThrownBy(() -> ruleService.getRuleById(ruleId))
                .isInstanceOf(RuleNotFoundException.class)
                .hasMessageContaining("Rule not found");
        }
    }

    @Nested
    @DisplayName("Create Rule Tests")
    class CreateRuleTests {
        @Test
        @DisplayName("Should create valid rule")
        void shouldCreateValidRule() {
            DealRule rule = DealRule.builder()
                .name("New Rule")
                .description("Test Description")
                .active(true)
                .build();
            when(ruleCachingService.saveRule(any(DealRule.class))).thenReturn(rule);

            DealRule result = ruleService.createRule(rule);

            assertThat(result).isNotNull()
                .satisfies(r -> {
                    assertThat(r.getName()).isEqualTo("New Rule");
                    assertThat(r.isActive()).isTrue();
                });
        }

        @Test
        @DisplayName("Should throw exception when rule is null")
        void shouldThrowExceptionWhenRuleNull() {
            assertThatThrownBy(() -> ruleService.createRule(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rule cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when required rule is inactive")
        void shouldThrowExceptionWhenRequiredRuleInactive() {
            DealRule rule = DealRule.builder()
                .name("Invalid Rule")
                .required(true)
                .active(false)
                .build();

            assertThatThrownBy(() -> ruleService.createRule(rule))
                .isInstanceOf(RuleValidationException.class)
                .hasMessageContaining("Required rule must be active");
        }
    }

    @Nested
    @DisplayName("Update Rule Tests")
    class UpdateRuleTests {
        @Test
        @DisplayName("Should update rule successfully")
        void shouldUpdateRule() {
            DealRule rule = DealRule.builder()
                .id("test-id")
                .name("Updated Rule")
                .active(true)
                .build();
            String modifiedBy = "test-user";
            String changeDescription = "Test update";

            ruleService.updateRule(rule, modifiedBy, changeDescription);

            verify(versioningService).createVersion(rule, modifiedBy, changeDescription);
        }

        @Test
        @DisplayName("Should throw exception when deactivating required rule")
        void shouldThrowExceptionWhenDeactivatingRequiredRule() {
            DealRule rule = DealRule.builder()
                .id("test-id")
                .name("Required Rule")
                .required(true)
                .active(false)
                .build();

            assertThatThrownBy(() -> ruleService.updateRule(rule, "user", "test"))
                .isInstanceOf(RuleValidationException.class)
                .hasMessageContaining("Cannot deactivate required rule");
        }
    }

    @Nested
    @DisplayName("Delete Rule Tests")
    class DeleteRuleTests {
        @Test
        @DisplayName("Should delete rule successfully")
        void shouldDeleteRule() {
            String ruleId = "test-rule";

            ruleService.deleteRule(ruleId);

            verify(ruleCachingService).invalidateRule(ruleId);
        }

        @Test
        @DisplayName("Should throw exception when rule ID is empty")
        void shouldThrowExceptionWhenRuleIdEmpty() {
            assertThatThrownBy(() -> ruleService.deleteRule(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rule ID cannot be empty");
        }
    }

    @Nested
    @DisplayName("Version Management Tests")
    class VersionManagementTests {
        @Test
        @DisplayName("Should rollback rule successfully")
        void shouldRollbackRule() {
            String ruleId = "test-rule";
            int version = 1;
            DealRule expectedRule = DealRule.builder()
                .id(ruleId)
                .version(version)
                .build();
            when(versioningService.rollbackToVersion(ruleId, version)).thenReturn(expectedRule);

            DealRule result = ruleService.rollbackRule(ruleId, version);

            assertThat(result)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.getId()).isEqualTo(ruleId);
                    assertThat(r.getVersion()).isEqualTo(version);
                });
        }

        @Test
        @DisplayName("Should throw exception when version is invalid")
        void shouldThrowExceptionWhenVersionInvalid() {
            assertThatThrownBy(() -> ruleService.rollbackRule("test-id", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Version must be greater than 0");
        }
    }
} 