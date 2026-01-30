package com.aciworldwide.dealdesk.rules.core;

import com.aciworldwide.dealdesk.model.Deal;
import org.jeasy.rules.api.Facts;
import org.junit.jupiter.api.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DynamicRuleTest {

    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Test
    void testSpELInjectionVulnerabilityInExecute() throws Exception {
        // This expression tries to access a system property, which requires StandardEvaluationContext
        String maliciousExpression = "T(java.lang.System).getProperty('java.version')";

        Expression condition = parser.parseExpression("true");
        Expression action = parser.parseExpression(maliciousExpression);

        DynamicRule rule = new DynamicRule("TestRule", "Test Description", 1, condition, action);

        Facts facts = new Facts();
        facts.put("deal", new Deal());

        // This should fail with SimpleEvaluationContext (SECURE)
        // It will throw a SpelEvaluationException because T(...) is not allowed
        assertThatThrownBy(() -> rule.execute(facts))
            .isInstanceOf(SpelEvaluationException.class)
            .hasMessageContaining("Type references are not supported");
    }

    @Test
    void testSpELInjectionVulnerabilityInEvaluate() {
        // This expression tries to access a system property in the condition
        String maliciousCondition = "T(java.lang.System).getProperty('java.version') != null";

        Expression condition = parser.parseExpression(maliciousCondition);
        Expression action = parser.parseExpression("true");

        DynamicRule rule = new DynamicRule("TestRule", "Test Description", 1, condition, action);

        Facts facts = new Facts();
        facts.put("deal", new Deal());

        // The evaluate method should return false when an exception occurs
        // (it catches exceptions and returns false instead of throwing)
        boolean result = rule.evaluate(facts);
        assertThat(result).isFalse();
    }

    @Test
    void testLegitimateExpressionInExecute() throws Exception {
        // Test that legitimate expressions still work correctly
        Deal deal = new Deal();
        deal.setValue(BigDecimal.valueOf(5000));

        Expression condition = parser.parseExpression("true");
        Expression action = parser.parseExpression("#deal.value = #deal.value * 1.1");

        DynamicRule rule = new DynamicRule("TestRule", "Test Description", 1, condition, action);

        Facts facts = new Facts();
        facts.put("deal", deal);

        // This should execute successfully
        rule.execute(facts);

        // Verify the action was executed
        assertThat(deal.getValue()).isEqualByComparingTo(BigDecimal.valueOf(5500));
    }

    @Test
    void testLegitimateExpressionInEvaluate() {
        // Test that legitimate condition expressions work correctly
        Deal deal = new Deal();
        deal.setValue(BigDecimal.valueOf(10000));

        Expression condition = parser.parseExpression("#deal.value > 5000");
        Expression action = parser.parseExpression("true");

        DynamicRule rule = new DynamicRule("TestRule", "Test Description", 1, condition, action);

        Facts facts = new Facts();
        facts.put("deal", deal);

        // This should evaluate successfully
        boolean result = rule.evaluate(facts);
        assertThat(result).isTrue();
    }

    @Test
    void testConditionCannotMutateState() {
        // Test that conditions use read-only context and cannot mutate state
        Deal deal = new Deal();
        deal.setValue(BigDecimal.valueOf(1000));

        // This condition tries to modify the deal value (should fail with read-only context)
        Expression condition = parser.parseExpression("#deal.value = 2000");
        Expression action = parser.parseExpression("true");

        DynamicRule rule = new DynamicRule("TestRule", "Test Description", 1, condition, action);

        Facts facts = new Facts();
        facts.put("deal", deal);

        // The evaluate method should return false when an exception occurs
        boolean result = rule.evaluate(facts);
        assertThat(result).isFalse();

        // Verify the value was not changed
        assertThat(deal.getValue()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }
}
