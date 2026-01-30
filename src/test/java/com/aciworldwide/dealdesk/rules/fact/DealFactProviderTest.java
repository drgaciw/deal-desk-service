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
    void provideFacts_shouldIncludeAveragePayment() {
        // Given
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        pricingModel.setAveragePayment(new BigDecimal("150.00"));
        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();

        // When
        dealFactProvider.provideFacts(deal, facts);

        // Then
        BigDecimal averagePayment = facts.get("averagePayment");
        assertThat(averagePayment).isNotNull();
        assertThat(averagePayment).isEqualByComparingTo(new BigDecimal("150.00"));
    }
}
