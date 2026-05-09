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
    void provideFactsShouldSetBaseFacts() {
        Deal deal = new Deal();
        deal.setPricingModel(new PricingModel());

        Facts facts = new Facts();
        dealFactProvider.provideFacts(deal, facts);

        assertThat((Object) facts.get("deal")).isEqualTo(deal);
        assertThat((Boolean) facts.get("rulesApplied")).isFalse();
    }

    @Test
    void provideFactsShouldExposeStoredPercentagesAsWholePercentValues() {
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCommercialCreditPercentage(new BigDecimal("0.105"));
        pricingModel.setAllCreditPercentage(new BigDecimal("0.20"));
        pricingModel.setDebitPercentage(new BigDecimal("0.305"));
        pricingModel.setDurbinRegulatedPercentage(new BigDecimal("0.40"));
        pricingModel.setAveragePayment(new BigDecimal("150.00"));
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();
        dealFactProvider.provideFacts(deal, facts);

        assertThat((BigDecimal) facts.get("commercialCreditPercentage")).isEqualByComparingTo(new BigDecimal("10.5"));
        assertThat((BigDecimal) facts.get("allCreditPercentage")).isEqualByComparingTo(new BigDecimal("20.0"));
        assertThat((BigDecimal) facts.get("debitPercentage")).isEqualByComparingTo(new BigDecimal("30.5"));
        assertThat((BigDecimal) facts.get("durbinRegPercentage")).isEqualByComparingTo(new BigDecimal("40.0"));
        assertThat((BigDecimal) facts.get("averagePayment")).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void provideFactsShouldDeriveDebitPercentageFromCreditPercentageWhenDebitIsMissing() {
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCreditPercentage(new BigDecimal("0.0275"));
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();
        dealFactProvider.provideFacts(deal, facts);

        assertThat((BigDecimal) facts.get("debitPercentage")).isEqualByComparingTo(new BigDecimal("97.25"));
    }

    @Test
    void provideFactsShouldPreferStoredDebitPercentageOverDerivedValue() {
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCreditPercentage(new BigDecimal("0.90"));
        pricingModel.setDebitPercentage(new BigDecimal("0.25"));
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();
        dealFactProvider.provideFacts(deal, facts);

        assertThat((BigDecimal) facts.get("debitPercentage")).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    void provideFactsShouldReturnZeroWhenPricingModelValuesAreNull() {
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
    void provideFactsShouldNotSetTransactionFactsWhenPricingModelIsNull() {
        Deal deal = new Deal();
        deal.setPricingModel(null);

        Facts facts = new Facts();
        dealFactProvider.provideFacts(deal, facts);

        assertThat((Object) facts.get("deal")).isEqualTo(deal);
        assertThat((Boolean) facts.get("rulesApplied")).isFalse();
        assertThat((Object) facts.get("commercialCreditPercentage")).isNull();
        assertThat((Object) facts.get("allCreditPercentage")).isNull();
        assertThat((Object) facts.get("debitPercentage")).isNull();
        assertThat((Object) facts.get("durbinRegPercentage")).isNull();
        assertThat((Object) facts.get("averagePayment")).isNull();
    }

    @Test
    void provideFacts_shouldRejectUnsupportedContext() {
        assertThatThrownBy(() -> dealFactProvider.provideFacts(new Object(), new Facts()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Context must be a Deal instance");
    }
}
