package com.aciworldwide.dealdesk.rules.engine.drools;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.rules.model.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DroolsRuleEngineTest {

    private DroolsRuleEngine ruleEngine;

    @BeforeEach
    void setUp() {
        ruleEngine = new DroolsRuleEngine();
        ruleEngine.initialize();
    }

    @Test
    void shouldAddRuleAndEvaluate() {
        // Given
        Rule rule = Rule.builder()
                .id("test-rule-1")
                .name("Test Rule")
                .condition("value > 100")
                .action("$deal.setNotes(\"High Value Deal\");")
                .active(true)
                .build();

        Deal deal = Deal.builder()
                .id("deal-1")
                .value(new BigDecimal("200"))
                .build();

        // When
        ruleEngine.addRule(rule);
        ruleEngine.evaluateRules(deal);

        // Then
        assertThat(deal.getNotes()).isEqualTo("High Value Deal");
    }

    @Test
    void shouldNotFireRuleWhenConditionNotMet() {
         // Given
        Rule rule = Rule.builder()
                .id("test-rule-2")
                .name("Test Rule 2")
                .condition("value > 100")
                .action("$deal.setNotes(\"High Value Deal\");")
                .active(true)
                .build();

        Deal deal = Deal.builder()
                .id("deal-2")
                .value(new BigDecimal("50"))
                .build();

        // When
        ruleEngine.addRule(rule);
        ruleEngine.evaluateRules(deal);

        // Then
        assertThat(deal.getNotes()).isNull();
    }
}
