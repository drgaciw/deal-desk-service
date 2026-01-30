package com.aciworldwide.dealdesk.rules.fact;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.tcv.PricingModel;
import org.jeasy.rules.api.Facts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Deal Fact Provider Tests")
class DealFactProviderTest {

    private DealFactProvider factProvider;
    private Facts facts;
    private Deal deal;

    @BeforeEach
    void setUp() {
        factProvider = new DealFactProvider();
        facts = new Facts();
        deal = new Deal();
    }

    @Test
    @DisplayName("Should verify supported context type is Deal")
    void shouldVerifySupportedContextType() {
        assertThat(factProvider.getSupportedContextType()).isEqualTo(Deal.class);
    }

    @Test
    @DisplayName("Should throw exception when context is not a Deal")
    void shouldThrowExceptionForInvalidContext() {
        assertThatThrownBy(() -> factProvider.provideFacts(new Object(), facts))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Context must be a Deal instance");
    }

    @Test
    @DisplayName("Should provide facts correctly when pricing model is populated")
    void shouldProvideFactsWithPricingModel() {
        // Arrange
        PricingModel pricingModel = new PricingModel();
        BigDecimal comCredit = new BigDecimal("10.0");
        BigDecimal allCredit = new BigDecimal("20.0");
        BigDecimal debit = new BigDecimal("30.0");
        BigDecimal durbin = new BigDecimal("40.0");
        BigDecimal avgPayment = new BigDecimal("100.00");

        pricingModel.setCommercialCreditPercentage(comCredit);
        pricingModel.setAllCreditPercentage(allCredit);
        pricingModel.setDebitPercentage(debit);
        pricingModel.setDurbinRegulatedPercentage(durbin);
        pricingModel.setAveragePayment(avgPayment);

        deal.setPricingModel(pricingModel);

        // Act
        factProvider.provideFacts(deal, facts);

        // Assert
        assertThat((Deal) facts.get("deal")).isEqualTo(deal);
        assertThat((BigDecimal) facts.get("commercialCreditPercentage")).isEqualTo(comCredit);
        assertThat((BigDecimal) facts.get("allCreditPercentage")).isEqualTo(allCredit);
        assertThat((BigDecimal) facts.get("debitPercentage")).isEqualTo(debit);
        assertThat((BigDecimal) facts.get("durbinRegPercentage")).isEqualTo(durbin);
        assertThat((BigDecimal) facts.get("averagePayment")).isEqualTo(avgPayment);
        assertThat((Boolean) facts.get("rulesApplied")).isEqualTo(false);
    }

    @Test
    @DisplayName("Should provide zero values when pricing model fields are null")
    void shouldProvideZeroValuesWhenFieldsAreNull() {
        // Arrange
        PricingModel pricingModel = new PricingModel();
        deal.setPricingModel(pricingModel);

        // Act
        factProvider.provideFacts(deal, facts);

        // Assert
        assertThat((BigDecimal) facts.get("commercialCreditPercentage")).isEqualTo(BigDecimal.ZERO);
        assertThat((BigDecimal) facts.get("allCreditPercentage")).isEqualTo(BigDecimal.ZERO);
        assertThat((BigDecimal) facts.get("debitPercentage")).isEqualTo(BigDecimal.ZERO);
        assertThat((BigDecimal) facts.get("durbinRegPercentage")).isEqualTo(BigDecimal.ZERO);
        assertThat((BigDecimal) facts.get("averagePayment")).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should not provide percentages when pricing model is null")
    void shouldNotProvidePercentagesWhenPricingModelIsNull() {
        // Arrange
        deal.setPricingModel(null);

        // Act
        factProvider.provideFacts(deal, facts);

        // Assert
        assertThat((Deal) facts.get("deal")).isEqualTo(deal);
        assertThat(facts.asMap()).doesNotContainKey("commercialCreditPercentage");
        assertThat(facts.asMap()).doesNotContainKey("allCreditPercentage");
        assertThat(facts.asMap()).doesNotContainKey("debitPercentage");
        assertThat(facts.asMap()).doesNotContainKey("durbinRegPercentage");
        assertThat(facts.asMap()).doesNotContainKey("averagePayment");
        assertThat((Boolean) facts.get("rulesApplied")).isEqualTo(false);
    }
}
