package com.aciworldwide.dealdesk.rules.impl;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.tcv.PricingModel;
import org.jeasy.rules.api.Facts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentThresholdRuleTest {

    private PaymentThresholdRule paymentThresholdRule;
    private Facts facts;
    private Deal deal;

    @BeforeEach
    void setUp() {
        paymentThresholdRule = new PaymentThresholdRule();
        facts = new Facts();
        deal = new Deal();
        deal.setPricingModel(new PricingModel());
    }

    @Test
    void testRuleEvaluationBelowThreshold() {
        // Setup
        deal.getPricingModel().setAveragePayment(new BigDecimal("4999.99"));
        facts.put("deal", deal);

        // Execute & Verify
        assertTrue(paymentThresholdRule.shouldApply(facts.get("deal")),
            "Rule should evaluate to true when below threshold");
    }

    @Test
    void testRuleEvaluationAtThreshold() {
        // Setup
        deal.getPricingModel().setAveragePayment(new BigDecimal("5000.00"));
        facts.put("deal", deal);

        // Execute & Verify
        assertFalse(paymentThresholdRule.shouldApply(facts.get("deal")),
            "Rule should evaluate to false at threshold");
    }

    @Test
    void testRuleEvaluationAboveThreshold() {
        // Setup
        deal.getPricingModel().setAveragePayment(new BigDecimal("5000.01"));
        facts.put("deal", deal);

        // Execute & Verify
        assertFalse(paymentThresholdRule.shouldApply(facts.get("deal")),
            "Rule should evaluate to false above threshold");
    }

    @Test
    void testRuleActionExecution() {
        // Setup
        deal.getPricingModel().setAveragePayment(new BigDecimal("4999.99"));
        facts.put("deal", deal);

        // Execute
        paymentThresholdRule.applyThreshold(facts);

        // Verify - Check for any side effects or logging
        // (This would need proper verification based on actual implementation)
    }
}