package com.aciworldwide.dealdesk.rules.impl;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("Profitability Rules Tests")
class ProfitabilityRulesTest {

    private RulesEngine rulesEngine;
    private Rules rules;
    private Facts facts;
    private double[] result;

    @BeforeEach
    void setUp() {
        rulesEngine = new DefaultRulesEngine();
        rules = new Rules();
        facts = new Facts();
        result = new double[1];

        // Register all profitability rules
        rules.register(new ProfitabilityRules.CalculateGrossRevenue());
        rules.register(new ProfitabilityRules.CalculateMarginPercentage());
        rules.register(new ProfitabilityRules.CalculateNetRevenuePerTransaction());
        rules.register(new ProfitabilityRules.CalculateUpliftDecrease());
    }

    @Test
    @DisplayName("Should calculate gross revenue correctly")
    void shouldCalculateGrossRevenueCorrectly() {
        // Arrange
        double settledPmtAmt = 1000.00;
        double netRevenue = 750.00;
        facts.put("settledPmtAmt", settledPmtAmt);
        facts.put("netRevenue", netRevenue);
        facts.put("grossRevenue", result);

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        assertThat(result[0]).isEqualTo(settledPmtAmt - netRevenue);
    }

    @ParameterizedTest
    @CsvSource({
        "1000.00, 750.00, 75.0",  // Standard case
        "2000.00, 1500.00, 75.0", // Different values, same percentage
        "100.00, 25.00, 25.0"     // Lower values
    })
    @DisplayName("Should calculate margin percentage correctly")
    void shouldCalculateMarginPercentageCorrectly(double grossRevenue, double netRevenue, double expectedMargin) {
        // Arrange
        facts.put("grossRevenue", grossRevenue);
        facts.put("netRevenue", netRevenue);
        facts.put("marginPercentage", result);

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        assertThat(result[0]).isEqualTo(expectedMargin, within(0.001));
    }

    @Test
    @DisplayName("Should calculate net revenue per transaction correctly")
    void shouldCalculateNetRevenuePerTransactionCorrectly() {
        // Arrange
        double netRevenue = 1000.00;
        int trxnCount = 4;
        facts.put("netRevenue", netRevenue);
        facts.put("trxnCount", trxnCount);
        facts.put("nrPerTrans", result);

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        assertThat(result[0]).isEqualTo(netRevenue / trxnCount);
    }

    @Test
    @DisplayName("Should calculate uplift/decrease correctly")
    void shouldCalculateUpliftDecreaseCorrectly() {
        // Arrange
        double netRevenueCard = 1000.00;
        double netRevenueACH = 750.00;
        facts.put("netRevenueCard", netRevenueCard);
        facts.put("netRevenueACH", netRevenueACH);
        facts.put("upliftDecrease", result);

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        assertThat(result[0]).isEqualTo(netRevenueCard - netRevenueACH);
    }

    @Test
    @DisplayName("Should not calculate gross revenue when inputs are invalid")
    void shouldNotCalculateGrossRevenueWhenInputsAreInvalid() {
        // Arrange
        facts.put("settledPmtAmt", 0.0);
        facts.put("netRevenue", 750.00);
        facts.put("grossRevenue", result);
        result[0] = 999.99; // Set initial value

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        assertThat(result[0]).isEqualTo(999.99); // Should remain unchanged
    }

    @Test
    @DisplayName("Should not calculate margin percentage when gross revenue is zero")
    void shouldNotCalculateMarginPercentageWhenGrossRevenueIsZero() {
        // Arrange
        facts.put("grossRevenue", 0.0);
        facts.put("netRevenue", 750.00);
        facts.put("marginPercentage", result);
        result[0] = 999.99; // Set initial value

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        assertThat(result[0]).isEqualTo(999.99); // Should remain unchanged
    }

    @Test
    @DisplayName("Should not calculate net revenue per transaction when transaction count is zero")
    void shouldNotCalculateNetRevenuePerTransactionWhenTrxnCountIsZero() {
        // Arrange
        facts.put("netRevenue", 1000.00);
        facts.put("trxnCount", 0);
        facts.put("nrPerTrans", result);
        result[0] = 999.99; // Set initial value

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        assertThat(result[0]).isEqualTo(999.99); // Should remain unchanged
    }

    @Test
    @DisplayName("Should calculate uplift/decrease with zero ACH revenue")
    void shouldCalculateUpliftDecreaseWithZeroACHRevenue() {
        // Arrange
        double netRevenueCard = 1000.00;
        double netRevenueACH = 0.00;
        facts.put("netRevenueCard", netRevenueCard);
        facts.put("netRevenueACH", netRevenueACH);
        facts.put("upliftDecrease", result);

        // Act
        rulesEngine.fire(rules, facts);

        // Assert
        assertThat(result[0]).isEqualTo(netRevenueCard);
    }
}
