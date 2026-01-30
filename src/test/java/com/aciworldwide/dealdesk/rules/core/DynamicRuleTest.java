package com.aciworldwide.dealdesk.rules.core;

import com.aciworldwide.dealdesk.model.Deal;
import org.jeasy.rules.api.Facts;
import org.junit.jupiter.api.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DynamicRuleTest {

    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Test
    void testSpELInjectionVulnerability() throws Exception {
        // This expression tries to access a system property, which requires StandardEvaluationContext
        String maliciousExpression = "T(java.lang.System).getProperty('java.version')";

        Expression condition = parser.parseExpression("true");
        Expression action = parser.parseExpression(maliciousExpression);

        DynamicRule rule = new DynamicRule("TestRule", "Test Description", 1, condition, action);

        Facts facts = new Facts();
        facts.put("deal", new Deal());

        // This should fail with SimpleEvaluationContext (SECURE)
        // It will throw an exception because T(...) is not allowed
        assertThatThrownBy(() -> rule.execute(facts))
            .isInstanceOf(Exception.class);
    }
}
