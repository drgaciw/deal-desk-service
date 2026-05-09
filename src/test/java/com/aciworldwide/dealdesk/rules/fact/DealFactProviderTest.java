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
    void provideFacts_shouldCalculatePercentagesAndAveragePayment() {
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setCreditPercentage(new BigDecimal("0.0275"));
        pricingModel.setCommercialCreditPercentage(new BigDecimal("0.05"));
        pricingModel.setAllCreditPercentage(new BigDecimal("0.55"));
        pricingModel.setDurbinRegulatedPercentage(new BigDecimal("0.45"));
        pricingModel.setAveragePayment(new BigDecimal("150.00"));
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        dealFactProvider.provideFacts(deal, facts);

        assertThat(facts.<Deal>get("deal")).isSameAs(deal);
        assertThat(facts.<BigDecimal>get("debitPercentage")).isEqualByComparingTo(new BigDecimal("97.25"));
        assertThat(facts.<BigDecimal>get("commercialCreditPercentage")).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(facts.<BigDecimal>get("allCreditPercentage")).isEqualByComparingTo(new BigDecimal("55.00"));
        assertThat(facts.<BigDecimal>get("durbinRegPercentage")).isEqualByComparingTo(new BigDecimal("45.00"));
        assertThat(facts.<BigDecimal>get("averagePayment")).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(facts.<Boolean>get("rulesApplied")).isFalse();
    }

    @Test
    void provideFacts_shouldDefaultMissingPercentagesToZero() {
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        dealFactProvider.provideFacts(deal, facts);

        assertThat(facts.<BigDecimal>get("debitPercentage")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(facts.<BigDecimal>get("commercialCreditPercentage")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(facts.<BigDecimal>get("allCreditPercentage")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(facts.<BigDecimal>get("durbinRegPercentage")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(facts.<BigDecimal>get("averagePayment")).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void provideFacts_shouldNotSetPricingFactsWhenPricingModelIsNull() {
        Deal deal = new Deal();
        deal.setPricingModel(null);

        Facts facts = new Facts();

        dealFactProvider.provideFacts(deal, facts);

        assertThat(facts.<Deal>get("deal")).isSameAs(deal);
        assertThat(facts.<BigDecimal>get("debitPercentage")).isNull();
        assertThat(facts.<BigDecimal>get("commercialCreditPercentage")).isNull();
        assertThat(facts.<BigDecimal>get("allCreditPercentage")).isNull();
        assertThat(facts.<BigDecimal>get("durbinRegPercentage")).isNull();
        assertThat(facts.<BigDecimal>get("averagePayment")).isNull();
        assertThat(facts.<Boolean>get("rulesApplied")).isFalse();
    }

    @Test
    void getSupportedContextType_shouldReturnDealClass() {
        assertThat(dealFactProvider.getSupportedContextType()).isEqualTo(Deal.class);
    }

    @Test
    void provideFacts_shouldRejectUnsupportedContext() {
        Facts facts = new Facts();

        assertThatThrownBy(() -> dealFactProvider.provideFacts(new Object(), facts))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Context must be a Deal instance");
    }
}
