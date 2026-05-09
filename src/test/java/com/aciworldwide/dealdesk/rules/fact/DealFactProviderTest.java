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
    @DisplayName("Should provide whole percentage facts when pricing model contains whole percentages")
    void shouldProvideFactsWithWholePercentages() {
        PricingModel pricingModel = new PricingModel();
        BigDecimal commercialCredit = new BigDecimal("10.0");
        BigDecimal allCredit = new BigDecimal("20.0");
        BigDecimal debit = new BigDecimal("30.0");
        BigDecimal durbin = new BigDecimal("40.0");
        BigDecimal averagePayment = new BigDecimal("100.00");

        pricingModel.setCommercialCreditPercentage(commercialCredit);
        pricingModel.setAllCreditPercentage(allCredit);
        pricingModel.setDebitPercentage(debit);
        pricingModel.setDurbinRegulatedPercentage(durbin);
        pricingModel.setAveragePayment(averagePayment);
        deal.setPricingModel(pricingModel);

        factProvider.provideFacts(deal, facts);

        assertThat((Deal) facts.get("deal")).isEqualTo(deal);
        assertThat((BigDecimal) facts.get("commercialCreditPercentage")).isEqualByComparingTo(commercialCredit);
        assertThat((BigDecimal) facts.get("allCreditPercentage")).isEqualByComparingTo(allCredit);
        assertThat((BigDecimal) facts.get("debitPercentage")).isEqualByComparingTo(debit);
        assertThat((BigDecimal) facts.get("durbinRegPercentage")).isEqualByComparingTo(durbin);
        assertThat((BigDecimal) facts.get("averagePayment")).isEqualByComparingTo(averagePayment);
        assertThat((Boolean) facts.get("rulesApplied")).isFalse();
    }

    @Test
    @DisplayName("Should convert fractional percentages to whole percentage facts")
    void shouldConvertFractionalPercentagesToWholePercentages() {
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCommercialCreditPercentage(new BigDecimal("0.10"));
        pricingModel.setAllCreditPercentage(new BigDecimal("0.55"));
        pricingModel.setDebitPercentage(new BigDecimal("0.30"));
        pricingModel.setDurbinRegulatedPercentage(new BigDecimal("0.45"));
        deal.setPricingModel(pricingModel);

        factProvider.provideFacts(deal, facts);

        assertThat((BigDecimal) facts.get("commercialCreditPercentage")).isEqualByComparingTo(new BigDecimal("10.0"));
        assertThat((BigDecimal) facts.get("allCreditPercentage")).isEqualByComparingTo(new BigDecimal("55.0"));
        assertThat((BigDecimal) facts.get("debitPercentage")).isEqualByComparingTo(new BigDecimal("30.0"));
        assertThat((BigDecimal) facts.get("durbinRegPercentage")).isEqualByComparingTo(new BigDecimal("45.0"));
    }

    @Test
    @DisplayName("Should calculate debit percentage from credit percentage when debit is not set")
    void shouldCalculateDebitPercentageFromCreditPercentage() {
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCreditPercentage(new BigDecimal("0.0275"));
        deal.setPricingModel(pricingModel);

        factProvider.provideFacts(deal, facts);

        assertThat((BigDecimal) facts.get("debitPercentage")).isEqualByComparingTo(new BigDecimal("97.25"));
    }

    @Test
    @DisplayName("Should calculate debit percentage from whole credit percentage")
    void shouldCalculateDebitPercentageFromWholeCreditPercentage() {
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCreditPercentage(new BigDecimal("80.0"));
        deal.setPricingModel(pricingModel);

        factProvider.provideFacts(deal, facts);

        assertThat((BigDecimal) facts.get("debitPercentage")).isEqualByComparingTo(new BigDecimal("20.0"));
    }

    @Test
    @DisplayName("Should prefer explicit debit percentage over credit-derived debit percentage")
    void shouldPreferExplicitDebitPercentage() {
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCreditPercentage(new BigDecimal("0.80"));
        pricingModel.setDebitPercentage(new BigDecimal("30.0"));
        deal.setPricingModel(pricingModel);

        factProvider.provideFacts(deal, facts);

        assertThat((BigDecimal) facts.get("debitPercentage")).isEqualByComparingTo(new BigDecimal("30.0"));
    }

    @Test
    @DisplayName("Should provide zero values when pricing model fields are null")
    void shouldProvideZeroValuesWhenFieldsAreNull() {
        PricingModel pricingModel = new PricingModel();
        deal.setPricingModel(pricingModel);

        factProvider.provideFacts(deal, facts);

        assertThat((BigDecimal) facts.get("commercialCreditPercentage")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat((BigDecimal) facts.get("allCreditPercentage")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat((BigDecimal) facts.get("debitPercentage")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat((BigDecimal) facts.get("durbinRegPercentage")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat((BigDecimal) facts.get("averagePayment")).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should not provide percentages when pricing model is null")
    void shouldNotProvidePercentagesWhenPricingModelIsNull() {
        deal.setPricingModel(null);

        factProvider.provideFacts(deal, facts);

        assertThat((Deal) facts.get("deal")).isEqualTo(deal);
        assertThat(facts.asMap()).doesNotContainKeys(
                "commercialCreditPercentage",
                "allCreditPercentage",
                "debitPercentage",
                "durbinRegPercentage",
                "averagePayment");
        assertThat((Boolean) facts.get("rulesApplied")).isFalse();
    }
}
