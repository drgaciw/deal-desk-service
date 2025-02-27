package com.aciworldwide.dealdesk.rules.service;

import com.aciworldwide.dealdesk.rules.model.RuleDefinition;
import com.aciworldwide.dealdesk.rules.repository.RuleDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleDefinitionServiceTest {

    @Mock
    private RuleDefinitionRepository ruleRepository;

    @InjectMocks
    private RuleDefinitionService ruleDefinitionService;

    private RuleDefinition validRule;

    @BeforeEach
    void setUp() {
        validRule = RuleDefinition.builder()
                .ruleKey("TEST_RULE")
                .name("Test Rule")
                .description("Test rule for unit tests")
                .category("TEST")
                .priority(100)
                .conditionExpression("true")
                .actionExpression("#deal.setValue(#deal.getValue().multiply(0.9))")
                .enabled(true)
                .parameters(new HashMap<>())
                .validFrom(LocalDateTime.now())
                .createdBy("test-user")
                .createdAt(LocalDateTime.now())
                .lastModifiedBy("test-user")
                .lastModifiedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createRule_WithValidRule_ShouldSucceed() {
        when(ruleRepository.existsByRuleKey(anyString())).thenReturn(false);
        when(ruleRepository.save(any(RuleDefinition.class))).thenReturn(validRule);

        RuleDefinition result = ruleDefinitionService.createRule(validRule);

        assertThat(result).isNotNull();
        assertThat(result.getRuleKey()).isEqualTo(validRule.getRuleKey());
        verify(ruleRepository).save(any(RuleDefinition.class));
    }

    @Test
    void createRule_WithDuplicateKey_ShouldThrowException() {
        when(ruleRepository.existsByRuleKey(anyString())).thenReturn(true);

        assertThatThrownBy(() -> ruleDefinitionService.createRule(validRule))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createRule_WithInvalidConditionExpression_ShouldThrowException() {
        validRule.setConditionExpression("invalid expression @#$");

        assertThatThrownBy(() -> ruleDefinitionService.createRule(validRule))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid condition expression");
    }

    @Test
    void createRule_WithInvalidActionExpression_ShouldThrowException() {
        validRule.setActionExpression("invalid expression @#$");

        assertThatThrownBy(() -> ruleDefinitionService.createRule(validRule))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid action expression");
    }

    @Test
    void updateRule_WithValidRule_ShouldSucceed() {
        when(ruleRepository.findByRuleKey(anyString())).thenReturn(Optional.of(validRule));
        when(ruleRepository.save(any(RuleDefinition.class))).thenReturn(validRule);

        RuleDefinition result = ruleDefinitionService.updateRule(validRule.getRuleKey(), validRule);

        assertThat(result).isNotNull();
        assertThat(result.getRuleKey()).isEqualTo(validRule.getRuleKey());
        verify(ruleRepository).save(any(RuleDefinition.class));
    }

    @Test
    void updateRule_WithNonExistentRule_ShouldThrowException() {
        when(ruleRepository.findByRuleKey(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ruleDefinitionService.updateRule("NON_EXISTENT", validRule))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteRule_WithExistingRule_ShouldSucceed() {
        when(ruleRepository.findByRuleKey(anyString())).thenReturn(Optional.of(validRule));
        doNothing().when(ruleRepository).delete(any(RuleDefinition.class));

        ruleDefinitionService.deleteRule(validRule.getRuleKey());

        verify(ruleRepository).delete(any(RuleDefinition.class));
    }

    @Test
    void deleteRule_WithNonExistentRule_ShouldThrowException() {
        when(ruleRepository.findByRuleKey(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ruleDefinitionService.deleteRule("NON_EXISTENT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getActiveRules_ShouldReturnActiveRules() {
        List<RuleDefinition> activeRules = List.of(validRule);
        when(ruleRepository.findActiveRulesByCategory(anyString(), any(LocalDateTime.class)))
                .thenReturn(activeRules);

        List<RuleDefinition> result = ruleDefinitionService.getActiveRules("TEST");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRuleKey()).isEqualTo(validRule.getRuleKey());
    }

    @Test
    void getRulesByCategory_ShouldReturnRules() {
        List<RuleDefinition> rules = List.of(validRule);
        when(ruleRepository.findByCategory(anyString())).thenReturn(rules);

        List<RuleDefinition> result = ruleDefinitionService.getRulesByCategory("TEST");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRuleKey()).isEqualTo(validRule.getRuleKey());
    }

    @Test
    void getActiveCategories_ShouldReturnCategories() {
        List<String> categories = List.of("TEST", "PROD");
        when(ruleRepository.findAllActiveCategories()).thenReturn(categories);

        List<String> result = ruleDefinitionService.getActiveCategories();

        assertThat(result).hasSize(2);
        assertThat(result).contains("TEST", "PROD");
    }
}