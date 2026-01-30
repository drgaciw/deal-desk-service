package com.aciworldwide.dealdesk.rules.fact;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.tcv.PricingModel;
import org.jeasy.rules.api.Facts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DealFactProviderTest {

    private DealFactProvider dealFactProvider;

    @BeforeEach
    void setUp() {
        dealFactProvider = new DealFactProvider();
    }

    @Test
    void provideFacts_shouldCalculatePercentages_whenPricingModelExists() {
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();

        // Set values
        pricingModel.setCommercialCreditPercentage(new BigDecimal("10.5"));
        pricingModel.setAllCreditPercentage(new BigDecimal("20.0"));
        pricingModel.setDebitPercentage(new BigDecimal("30.5"));
        pricingModel.setDurbinRegulatedPercentage(new BigDecimal("40.0"));
        pricingModel.setAveragePayment(new BigDecimal("150.00"));

        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();
        dealFactProvider.provideFacts(deal, facts);

        assertNotNull(facts.get("deal"));
        assertEquals(deal, facts.get("deal"));
        assertEquals(Boolean.FALSE, facts.get("rulesApplied"));

        // Verify values are correctly extracted
        assertEquals(new BigDecimal("10.5"), facts.get("commercialCreditPercentage"));
        assertEquals(new BigDecimal("20.0"), facts.get("allCreditPercentage"));
        assertEquals(new BigDecimal("30.5"), facts.get("debitPercentage"));
        assertEquals(new BigDecimal("40.0"), facts.get("durbinRegPercentage"));
        assertEquals(new BigDecimal("150.00"), facts.get("averagePayment"));
    }

    @Test
    void provideFacts_shouldReturnZero_whenPricingModelValuesAreNull() {
        Deal deal = new Deal();
        PricingModel pricingModel = new PricingModel();
        // Values not set, so they are null in PricingModel

        deal.setPricingModel(pricingModel);

        Facts facts = new Facts();
        dealFactProvider.provideFacts(deal, facts);

        assertEquals(BigDecimal.ZERO, facts.get("commercialCreditPercentage"));
        assertEquals(BigDecimal.ZERO, facts.get("allCreditPercentage"));
        assertEquals(BigDecimal.ZERO, facts.get("debitPercentage"));
        assertEquals(BigDecimal.ZERO, facts.get("durbinRegPercentage"));
        assertEquals(BigDecimal.ZERO, facts.get("averagePayment"));
    }
}
