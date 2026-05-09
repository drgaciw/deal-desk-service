package com.aciworldwide.dealdesk.rules.fact;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.tcv.PricingModel;
import org.jeasy.rules.api.Facts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DealFactProviderTest {

    private DealFactProvider dealFactProvider;

    @BeforeEach
    void setUp() {
        dealFactProvider = new DealFactProvider();
    }

    @Test
    void getSupportedContextType_shouldSupportDeal() {
        assertThat(dealFactProvider.getSupportedContextType()).isEqualTo(Deal.class);
    }

    @Test
    void provideFacts_shouldPopulateDealAndRulesApplied() {
        Deal deal = new Deal();
        deal.setPricingModel(new PricingModel());
        Facts facts = new Facts();

        dealFactProvider.provideFacts(deal, facts);

        assertThat((Object) facts.get("deal")).isEqualTo(deal);
        assertThat((Boolean) facts.get("rulesApplied")).isFalse();
    }

    @Test
    void provideFacts_shouldCalculateDebitPercentageCorrectly() {
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCreditPercentage(new BigDecimal("0.0275")); // 2.75%
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        dealFactProvider.provideFacts(deal, facts);

        BigDecimal debitPercentage = facts.get("debitPercentage");
        assertThat(debitPercentage).isNotNull();
        assertThat(debitPercentage).isEqualByComparingTo(new BigDecimal("97.25"));
    }

    @Test
    void provideFacts_shouldNotSetDebitPercentageWhenPricingModelIsNull() {
        Deal deal = new Deal();
        deal.setPricingModel(null);

        Facts facts = new Facts();

        dealFactProvider.provideFacts(deal, facts);

        assertThat((BigDecimal) facts.get("debitPercentage")).isNull();
        assertThat((Object) facts.get("deal")).isEqualTo(deal);
        assertThat((Boolean) facts.get("rulesApplied")).isFalse();
    }

    @Test
    void provideFacts_shouldReturnZeroWhenCreditPercentageIsNull() {
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCreditPercentage(null);
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        dealFactProvider.provideFacts(deal, facts);

        BigDecimal debitPercentage = facts.get("debitPercentage");
        assertThat(debitPercentage).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void provideFacts_shouldCalculateDebitPercentageForHighCredit() {
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCreditPercentage(new BigDecimal("0.8")); // 80%
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        dealFactProvider.provideFacts(deal, facts);

        BigDecimal debitPercentage = facts.get("debitPercentage");
        assertThat(debitPercentage).isNotNull();
        assertThat(debitPercentage).isEqualByComparingTo(new BigDecimal("20.0"));
    }

    @Test
    void provideFacts_shouldCalculateCommercialCreditPercentageCorrectly() {
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCommercialCreditPercentage(new BigDecimal("0.10")); // 10%
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        dealFactProvider.provideFacts(deal, facts);

        BigDecimal percentage = facts.get("commercialCreditPercentage");
        assertThat(percentage).isNotNull();
        assertThat(percentage).isEqualByComparingTo(new BigDecimal("10.0"));
    }

    @Test
    void provideFacts_shouldCalculateDurbinRegulatedPercentageCorrectly() {
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setDurbinRegulatedPercentage(new BigDecimal("0.15")); // 15%
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        dealFactProvider.provideFacts(deal, facts);

        BigDecimal percentage = facts.get("durbinRegPercentage");
        assertThat(percentage).isNotNull();
        assertThat(percentage).isEqualByComparingTo(new BigDecimal("15.0"));
    }

    @Test
    void provideFacts_shouldCalculateAllCreditPercentageCorrectly() {
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setAllCreditPercentage(new BigDecimal("0.55")); // 55%
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        dealFactProvider.provideFacts(deal, facts);

        BigDecimal allCreditPercentage = facts.get("allCreditPercentage");
        assertThat(allCreditPercentage).isNotNull();
        assertThat(allCreditPercentage).isEqualByComparingTo(new BigDecimal("55.0"));
    }

    @Test
    void provideFacts_shouldIncludeAveragePayment() {
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setAveragePayment(new BigDecimal("50.25"));
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        dealFactProvider.provideFacts(deal, facts);

        BigDecimal averagePayment = facts.get("averagePayment");
        assertThat(averagePayment).isNotNull();
        assertThat(averagePayment).isEqualByComparingTo(new BigDecimal("50.25"));
    }

    @Test
    void provideFacts_shouldReturnZeroWhenNullablePricingValuesAreNull() {
        Deal deal = new Deal();
        deal.setPricingModel(new PricingModel());

        Facts facts = new Facts();

        dealFactProvider.provideFacts(deal, facts);

        assertThat((BigDecimal) facts.get("commercialCreditPercentage")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat((BigDecimal) facts.get("allCreditPercentage")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat((BigDecimal) facts.get("debitPercentage")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat((BigDecimal) facts.get("durbinRegPercentage")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat((BigDecimal) facts.get("averagePayment")).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void provideFacts_shouldRejectUnsupportedContext() {
        assertThatThrownBy(() -> dealFactProvider.provideFacts(new Object(), new Facts()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Context must be a Deal instance");
    }
}
