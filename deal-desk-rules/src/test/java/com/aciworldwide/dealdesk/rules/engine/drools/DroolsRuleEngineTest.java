package com.aciworldwide.dealdesk.rules.engine.drools;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.rules.api.RuleDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieSession;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DroolsRuleEngineTest {

    @InjectMocks
    private DroolsRuleEngine droolsRuleEngine;

    @Mock
    private KieBase kieBase;

    @Mock
    private KiePackage kiePackage;

    @Mock
    private Rule droolsRule;

    @Mock
    private KieSession kieSession;

    @Test
    void testRemoveRule() {
        String ruleId = "testRule";
        String ruleName = "Test Rule";
        String packageName = "com.aciworldwide.dealdesk.rules";

        RuleDefinition ruleDefinition = mock(RuleDefinition.class);
        when(ruleDefinition.getId()).thenReturn(ruleId);
        when(ruleDefinition.getName()).thenReturn(ruleName);

        // Mock Drools internal structure
        when(kieBase.getKiePackages()).thenReturn(Collections.singletonList(kiePackage));
        when(kiePackage.getName()).thenReturn(packageName);
        when(kiePackage.getRules()).thenReturn(Collections.singletonList(droolsRule));
        when(droolsRule.getName()).thenReturn(ruleName);

        // Inject mock KieBase
        ReflectionTestUtils.setField(droolsRuleEngine, "kieBase", kieBase);

        // Setup internal state
        droolsRuleEngine.addRule(ruleDefinition);

        // Call removeRule
        droolsRuleEngine.removeRule(ruleId);

        // Verify
        verify(kieBase).removeRule(packageName, ruleName);
    }

    @Test
    void testEvaluateRules() {
        // Arrange
        Deal deal = mock(Deal.class);
        when(deal.getId()).thenReturn("deal-123");
        when(kieBase.newKieSession()).thenReturn(kieSession);

        // Inject mock KieBase
        ReflectionTestUtils.setField(droolsRuleEngine, "kieBase", kieBase);

        // Act
        droolsRuleEngine.evaluateRules(deal);

        // Assert
        verify(kieSession).insert(deal);
        verify(kieSession).fireAllRules();
        verify(kieSession).dispose();
    }
}
