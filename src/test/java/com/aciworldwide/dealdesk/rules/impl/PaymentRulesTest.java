package com.aciworldwide.dealdesk.rules.impl;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Payment Rules Tests")
class PaymentRulesTest {

    private RulesEngine rulesEngine;
    private Rules rules;
    private Facts facts;
    private double[] totalFee;

    @BeforeEach
    void setUp() {
        rulesEngine = new DefaultRulesEngine();
        rules = new Rules();
        facts = new Facts();
        totalFee = new double[1];

        // Register all payment rules
        rules.register(new PaymentRules.ComCreditRule());
        rules.register(new PaymentRules.ConsCreditRule());
        rules.register(new PaymentRules.ATMPinlessRule());
        rules.register(new PaymentRules.DebitRule());
        rules.register(new PaymentRules.ACHRule());
    }

    @Test
    @DisplayName("Should calculate Com Credit fee correctly")
    void shouldCalculateComCreditFeeCorrectly() {
        // Arrange
        double principal = 1000.00;
        facts.put("paymentType", "Com Credit");
        facts.put("principal", principal);
        facts.put("totalFee", totalFee);

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        assertThat(totalFee[0]).isEqualTo(principal * 0.0295);
    }

    @Test
    @DisplayName("Should calculate Cons Credit fee correctly")
    void shouldCalculateConsCreditFeeCorrectly() {
        // Arrange
        facts.put("paymentType", "Cons Credit");
        facts.put("totalFee", totalFee);

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        assertThat(totalFee[0]).isEqualTo(2.00);
    }

    @Test
    @DisplayName("Should calculate ATM/Pinless fee correctly")
    void shouldCalculateATMPinlessFeeCorrectly() {
        // Arrange
        double cost = 3.00;
        facts.put("paymentType", "ATM/Pinless");
        facts.put("cost", cost);
        facts.put("totalFee", totalFee);

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        assertThat(totalFee[0]).isEqualTo(cost + 7.00);
    }

    @Test
    @DisplayName("Should calculate Debit fee correctly")
    void shouldCalculateDebitFeeCorrectly() {
        // Arrange
        double cost = 3.00;
        double principal = 1000.00;
        facts.put("paymentType", "Debit");
        facts.put("cost", cost);
        facts.put("principal", principal);
        facts.put("totalFee", totalFee);

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        assertThat(totalFee[0]).isEqualTo(cost + (principal * 0.0175));
    }

    @Test
    @DisplayName("Should calculate ACH fee correctly")
    void shouldCalculateACHFeeCorrectly() {
        // Arrange
        facts.put("paymentType", "ACH");
        facts.put("totalFee", totalFee);

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        assertThat(totalFee[0]).isEqualTo(0.05);
    }

    @Test
    @DisplayName("Should not modify fee for unknown payment type")
    void shouldNotModifyFeeForUnknownPaymentType() {
        // Arrange
        facts.put("paymentType", "Unknown");
        facts.put("totalFee", totalFee);
        totalFee[0] = 10.00; // Set initial value

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        assertThat(totalFee[0]).isEqualTo(10.00); // Should remain unchanged
    }
}
