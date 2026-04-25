package com.aciworldwide.dealdesk.rules.fact;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.tcv.PricingModel;
import org.jeasy.rules.api.Facts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DealFactProviderTest {

    private DealFactProvider dealFactProvider;

    @BeforeEach
    void setUp() {
        dealFactProvider = new DealFactProvider();
    }

    @Test
    void provideFacts_shouldCalculateDebitPercentageCorrectly() {
        // Given
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCreditPercentage(new BigDecimal("0.0275")); // 2.75%
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        // When
        dealFactProvider.provideFacts(deal, facts);

        // Then
        BigDecimal debitPercentage = facts.get("debitPercentage");
        assertThat(debitPercentage).isNotNull();
        // 1 - 0.0275 = 0.9725. 0.9725 * 100 = 97.25
        assertThat(debitPercentage).isEqualByComparingTo(new BigDecimal("97.25"));
    }

    @Test
    void provideFacts_shouldNotSetDebitPercentageWhenPricingModelIsNull() {
        // Given
        Deal deal = new Deal();
        deal.setPricingModel(null);

        Facts facts = new Facts();

        // When
        dealFactProvider.provideFacts(deal, facts);

        // Then
        BigDecimal debitPercentage = facts.get("debitPercentage");
        // No facts added when PricingModel is null
        assertThat(debitPercentage).isNull();
    }

    @Test
    void provideFacts_shouldReturnZeroWhenCreditPercentageIsNull() {
        // Given
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCreditPercentage(null);
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        // When
        dealFactProvider.provideFacts(deal, facts);

        // Then
        BigDecimal debitPercentage = facts.get("debitPercentage");
        assertThat(debitPercentage).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void provideFacts_shouldCalculateDebitPercentageForHighCredit() {
        // Given
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCreditPercentage(new BigDecimal("0.8")); // 80%
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        // When
        dealFactProvider.provideFacts(deal, facts);

        // Then
        BigDecimal debitPercentage = facts.get("debitPercentage");
        assertThat(debitPercentage).isNotNull();
        // 1 - 0.8 = 0.2. 0.2 * 100 = 20.0
        assertThat(debitPercentage).isEqualByComparingTo(new BigDecimal("20.0"));
    }

    @Test
    void provideFacts_shouldCalculateDurbinRegulatedPercentageCorrectly() {
        // Given
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setDurbinRegulatedPercentage(new BigDecimal("0.45")); // 45%
    void provideFacts_shouldCalculateCommercialCreditPercentageCorrectly() {
        // Given
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCommercialCreditPercentage(new BigDecimal("0.05")); // 5%
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        // When
        dealFactProvider.provideFacts(deal, facts);

        // Then
        BigDecimal durbinRegPercentage = facts.get("durbinRegPercentage");
        assertThat(durbinRegPercentage).isNotNull();
        assertThat(durbinRegPercentage).isEqualByComparingTo(new BigDecimal("45.0"));
    }

    @Test
    void provideFacts_shouldCalculateCommercialCreditPercentageCorrectly() {
        // Given
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCommercialCreditPercentage(new BigDecimal("0.10")); // 10%
        BigDecimal percentage = facts.get("commercialCreditPercentage");
        assertThat(percentage).isNotNull();
        // 0.05 * 100 = 5.0
        assertThat(percentage).isEqualByComparingTo(new BigDecimal("5.0"));
    }

    @Test
    void provideFacts_shouldCalculateDurbinRegulatedPercentageCorrectly() {
        // Given
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setDurbinRegulatedPercentage(new BigDecimal("0.15")); // 15%
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        // When
        dealFactProvider.provideFacts(deal, facts);

        // Then
        BigDecimal commercialCreditPercentage = facts.get("commercialCreditPercentage");
        assertThat(commercialCreditPercentage).isNotNull();
        assertThat(commercialCreditPercentage).isEqualByComparingTo(new BigDecimal("10.0"));
    }

    @Test
    void provideFacts_shouldCalculateAllCreditPercentageCorrectly() {
        // Given
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setAllCreditPercentage(new BigDecimal("0.55")); // 55%
        BigDecimal percentage = facts.get("durbinRegPercentage");
        assertThat(percentage).isNotNull();
        // 0.15 * 100 = 15.0
        assertThat(percentage).isEqualByComparingTo(new BigDecimal("15.0"));
    }

    @Test
    void provideFacts_shouldCalculateAveragePaymentCorrectly() {
        // Given
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setAveragePayment(new BigDecimal("50.25"));
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        // When
        dealFactProvider.provideFacts(deal, facts);

        // Then
        BigDecimal allCreditPercentage = facts.get("allCreditPercentage");
        assertThat(allCreditPercentage).isNotNull();
        assertThat(allCreditPercentage).isEqualByComparingTo(new BigDecimal("55.0"));
    }

    @Test
    void provideFacts_shouldReturnZeroWhenNewPercentagesAreNull() {
        // Given
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        // Don't set any percentages (they are null by default)
        BigDecimal avgPayment = facts.get("averagePayment");
        assertThat(avgPayment).isNotNull();
        assertThat(avgPayment).isEqualByComparingTo(new BigDecimal("50.25"));
    }

    @Test
    void provideFacts_shouldCalculateAllCreditPercentageCorrectly() {
        // Given
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCreditPercentage(new BigDecimal("0.0275")); // 2.75%
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        // When
        dealFactProvider.provideFacts(deal, facts);

        // Then
        BigDecimal durbinRegPercentage = facts.get("durbinRegPercentage");
        assertThat(durbinRegPercentage).isEqualByComparingTo(BigDecimal.ZERO);

        BigDecimal commercialCreditPercentage = facts.get("commercialCreditPercentage");
        assertThat(commercialCreditPercentage).isEqualByComparingTo(BigDecimal.ZERO);

        BigDecimal allCreditPercentage = facts.get("allCreditPercentage");
        assertThat(allCreditPercentage).isEqualByComparingTo(BigDecimal.ZERO);
        BigDecimal percentage = facts.get("allCreditPercentage");
        assertThat(percentage).isNotNull();
        // 0.0275 * 100 = 2.75
        assertThat(percentage).isEqualByComparingTo(new BigDecimal("2.75"));
    }
}
