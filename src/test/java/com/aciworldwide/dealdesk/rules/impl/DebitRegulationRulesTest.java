package com.aciworldwide.dealdesk.rules.impl;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.tcv.PricingModel;
import com.aciworldwide.dealdesk.model.tcv.RepricingTriggers;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Debit Regulation Rules Tests")
class DebitRegulationRulesTest {

    private RulesEngine rulesEngine;
    private Rules rules;
    private Facts facts;
    private Deal deal;

    @BeforeEach
    void setUp() {
        rulesEngine = new DefaultRulesEngine();
        rules = new Rules();
        facts = new Facts();

        deal = new Deal();
        deal.setId("deal-123");
        deal.setValue(new BigDecimal("1000.00"));

        PricingModel pricingModel = new PricingModel();
        RepricingTriggers triggers = new RepricingTriggers();
        // Set up triggers so rules evaluate to true
        triggers.setDebitPercentageThreshold(new BigDecimal("50.00")); // Threshold 50%
        triggers.setDebitPercentageAbove(false); // Trigger if below 50%

        triggers.setDurbinRegPercentageThreshold(new BigDecimal("60.00")); // Threshold 60%
        triggers.setDurbinRegPercentageAbove(false); // Trigger if below 60%

        pricingModel.setRepricingTriggers(Collections.singletonList(triggers));
        deal.setPricingModel(pricingModel);

        facts.put("deal", deal);
        facts.put("rulesApplied", false);
        // By default, averagePayment is not in facts unless provided by FactProvider or explicitly added
        // In this test, we are bypassing FactProvider and putting facts manually.
        // So unless we put "averagePayment", it's null.
    }

    @Test
    @DisplayName("Should apply Debit Percentage Rule adjustment with fallback fixed fee")
    void shouldApplyDebitPercentageRuleAdjustment_FallbackFixedFee() {
        // Arrange
        rules.register(new DebitRegulationRules.DebitPercentageRule());

        // debitPercentage is 40% (below 50% threshold) -> should trigger
        facts.put("debitPercentage", new BigDecimal("40.00"));
        // averagePayment is ZERO -> fallback fixed fee
        facts.put("averagePayment", BigDecimal.ZERO);

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        // Proportional Adjustment: 1000 * 0.40 * 0.025 = 10.00
        // Fixed fee fallback: 0.21
        // Total: 1000 + 10 + 0.21 = 1010.21
        assertThat(deal.getValue()).isEqualByComparingTo(new BigDecimal("1010.21"));
    }

    @Test
    @DisplayName("Should apply Debit Percentage Rule adjustment with calculated fixed fee")
    void shouldApplyDebitPercentageRuleAdjustment_CalculatedFixedFee() {
        // Arrange
        rules.register(new DebitRegulationRules.DebitPercentageRule());

        // debitPercentage is 40%
        BigDecimal debitPercentage = new BigDecimal("40.00");
        facts.put("debitPercentage", debitPercentage);

        // averagePayment is 50.00
        BigDecimal averagePayment = new BigDecimal("50.00");
        facts.put("averagePayment", averagePayment);

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        // Proportional Adjustment: 1000 * 0.40 * 0.025 = 10.00

        // Fixed Fee Calculation:
        // Total Transactions = 1000 / 50 = 20
        // Affected Transactions = 20 * 0.40 = 8
        // Fixed Fee = 8 * 0.21 = 1.68

        // Total: 1000 + 10.00 + 1.68 = 1011.68
        assertThat(deal.getValue()).isEqualByComparingTo(new BigDecimal("1011.68"));
    }

    @Test
    @DisplayName("Should apply Durbin Regulated Rule adjustment with calculated fixed fee")
    void shouldApplyDurbinRegulatedRuleAdjustment_CalculatedFixedFee() {
        // Arrange
        rules.register(new DebitRegulationRules.DurbinRegulatedRule());

        // durbinRegPercentage is 55%
        facts.put("durbinRegPercentage", new BigDecimal("55.00"));

        // averagePayment is 100.00
        BigDecimal averagePayment = new BigDecimal("100.00");
        facts.put("averagePayment", averagePayment);

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        // Proportional Adjustment: 1000 * 0.55 * 0.03 = 16.50

        // Fixed Fee Calculation:
        // Total Transactions = 1000 / 100 = 10
        // Affected Transactions = 10 * 0.55 = 5.5 -> 5.5 * 0.21 = 1.155

        // Total: 1000 + 16.50 + 1.155 = 1017.655
        assertThat(deal.getValue()).isEqualByComparingTo(new BigDecimal("1017.655"));
    }
}
