package com.aciworldwide.dealdesk.rules.engine.drools;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.rules.model.Rule;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class DroolsRuleEngineTest {

    @Test
    void testEvaluateRules() {
        DroolsRuleEngine engine = new DroolsRuleEngine();
        engine.initialize();

        Deal deal = new Deal();
        deal.setId("deal-1");
        deal.setValue(new BigDecimal("50000"));

        Rule rule = Rule.builder()
                .id("rule-1")
                .name("High Value Rule")
                .condition("value > 10000")
                .action("deal.setNotes(\"High Value Deal\")")
                .priority(1)
                .active(true)
                .build();

        engine.addRule(rule);

        engine.evaluateRules(deal);

        assertEquals("High Value Deal", deal.getNotes());
    }
}
