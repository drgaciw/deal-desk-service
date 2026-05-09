package com.aciworldwide.dealdesk.rules.engine.drools;

import com.aciworldwide.dealdesk.rules.model.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.KieBase;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class DroolsRuleEngineTest {

    private DroolsRuleEngine droolsRuleEngine;

    @BeforeEach
    void setUp() {
        droolsRuleEngine = new DroolsRuleEngine();
    }

    @Test
    void testAddRule() {
        // Create a valid rule
        Rule rule = Rule.builder()
                .id("test-rule-1")
                .name("Test Rule")
                .description("A test rule")
                .active(true)
                .priority(1)
                .condition("$deal : Deal(status == DealStatus.DRAFT)")
                .action("System.out.println(\"Rule fired!\");")
                .build();

        // Add rule
        droolsRuleEngine.addRule(rule);

        // Verify rule is in the map
        assertEquals(1, droolsRuleEngine.getRules().size());
        assertTrue(droolsRuleEngine.getRules().contains(rule));

        // Verify KieBase was built
        KieBase kieBase = (KieBase) ReflectionTestUtils.getField(droolsRuleEngine, "kieBase");
        assertNotNull(kieBase, "KieBase should be initialized after adding a rule");

        // Verify rule exists in KieBase
        // Note: Package name matches what is generated in DroolsRuleEngine.generateDrl
        assertNotNull(kieBase.getKiePackage("com.aciworldwide.dealdesk.rules"), "Package 'com.aciworldwide.dealdesk.rules' should exist");
        assertFalse(kieBase.getKiePackage("com.aciworldwide.dealdesk.rules").getRules().isEmpty(), "Rules should be present in package");

        org.kie.api.definition.rule.Rule droolsRule = kieBase.getRule("com.aciworldwide.dealdesk.rules", "Test Rule");
        assertNotNull(droolsRule, "Drools rule 'Test Rule' should exist");
    }

    @Test
    void testRemoveRule() {
         // Create a valid rule
        Rule rule = Rule.builder()
                .id("test-rule-1")
                .name("Test Rule")
                .description("A test rule")
                .active(true)
                .priority(1)
                .condition("$deal : Deal(status == DealStatus.DRAFT)")
                .action("System.out.println(\"Rule fired!\");")
                .build();

        droolsRuleEngine.addRule(rule);
        assertEquals(1, droolsRuleEngine.getRules().size());

        droolsRuleEngine.removeRule("test-rule-1");
        assertEquals(0, droolsRuleEngine.getRules().size());

        KieBase kieBase = (KieBase) ReflectionTestUtils.getField(droolsRuleEngine, "kieBase");
        // After removing the only rule, the package might still exist but with no rules, or might disappear depending on Drools version/config.
        // We just check the internal map is empty.
        // If we want to check KieBase, we can check if rule is gone.
        if (kieBase != null && kieBase.getKiePackage("com.aciworldwide.dealdesk.rules") != null) {
             assertNull(kieBase.getRule("com.aciworldwide.dealdesk.rules", "Test Rule"), "Rule should be removed from KieBase");
        }
    }
}
