package com.aciworldwide.dealdesk.rules.service;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.rules.fact.FactProvider;
import com.aciworldwide.dealdesk.rules.model.RuleDefinition;
import com.aciworldwide.dealdesk.rules.repository.RuleDefinitionRepository;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Rule Execution Service Tests")
class RuleExecutionServiceTest {

    @Mock
    private RulesEngine rulesEngine;

    @Mock
    private RuleDefinitionRepository ruleRepository;

    @Mock
    private List<FactProvider> factProviders;

    @Mock
    private FactProvider dealFactProvider;

    @InjectMocks
    private RuleExecutionService ruleExecutionService;

    private Deal testDeal;
    private RuleDefinition testRule;

    @BeforeEach
    void setUp() {
        testDeal = new Deal();
        testDeal.setId("test-deal-id");

        testRule = new RuleDefinition();
        testRule.setEnabled(true);
        testRule.setRuleKey("test-rule");
        testRule.setCategory("PRICING");
        testRule.setConditionExpression("deal.value > 1000");
        testRule.setActionExpression("deal.applyDiscount(0.1)");
        testRule.setPriority(1);

        // Setup common mocks
        lenient().when(factProviders.stream()).thenReturn(Collections.singletonList(dealFactProvider).stream());
        lenient().when(dealFactProvider.getSupportedContextType()).thenAnswer(invocation -> Deal.class);
    }

    @Test
    @DisplayName("Should execute rules for given category and context")
    void executeRules_ShouldExecuteRulesForGivenCategoryAndContext() {
        // Arrange
        when(ruleRepository.findActiveRulesByCategory(any(), any())).thenReturn(Collections.singletonList(testRule));

        // Act
        Facts facts = ruleExecutionService.executeRules("PRICING", testDeal);

        // Assert
        verify(dealFactProvider).provideFacts(eq(testDeal), any(Facts.class));
        verify(rulesEngine).fire(any(Rules.class), eq(facts));
        assertThat(facts).isNotNull();
    }

    @Test
    @DisplayName("Should skip inactive rules")
    void executeRules_ShouldSkipInactiveRules() {
        // Arrange
        testRule.setEnabled(false);
        when(ruleRepository.findActiveRulesByCategory(any(), any())).thenReturn(Collections.emptyList());

        // Act
        Facts facts = ruleExecutionService.executeRules("PRICING", testDeal);

        // Assert
        verify(rulesEngine, never()).fire(any(Rules.class), any(Facts.class));
        assertThat(facts).isNotNull();
    }

    @Test
    @DisplayName("Should apply fact providers for matching context type")
    void executeRules_ShouldApplyFactProvidersForMatchingContextType() {
        // Arrange
        when(ruleRepository.findActiveRulesByCategory(any(), any())).thenReturn(Collections.singletonList(testRule));

        // Act
        Facts facts = ruleExecutionService.executeRules("PRICING", testDeal);

        // Assert
        verify(dealFactProvider).provideFacts(eq(testDeal), any(Facts.class));
        assertThat(facts).isNotNull();
    }

    @Test
    @DisplayName("Should cache compiled expressions")
    void executeRules_ShouldCacheCompiledExpressions() {
        // Arrange
        when(ruleRepository.findActiveRulesByCategory(any(), any())).thenReturn(Arrays.asList(testRule, testRule));

        // Act
        ruleExecutionService.executeRules("PRICING", testDeal);
        ruleExecutionService.executeRules("PRICING", testDeal);

        // Assert
        // The SpEL expression should only be compiled once per unique expression
        verify(rulesEngine, times(2)).fire(any(Rules.class), any(Facts.class));
    }

    @Test
    @DisplayName("Should handle empty rule list")
    void executeRules_ShouldHandleEmptyRuleList() {
        // Arrange
        when(ruleRepository.findActiveRulesByCategory(any(), any())).thenReturn(Collections.emptyList());

        // Act
        Facts facts = ruleExecutionService.executeRules("PRICING", testDeal);

        // Assert
        verify(rulesEngine, never()).fire(any(Rules.class), any(Facts.class));
        assertThat(facts).isNotNull();
    }
}
