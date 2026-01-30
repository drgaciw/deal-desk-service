package com.aciworldwide.dealdesk.rules.fact;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.tcv.PricingModel;
import org.jeasy.rules.api.Facts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DealFactProviderTest {

    private DealFactProvider dealFactProvider;
    private Facts facts;
    private Deal deal;

    @BeforeEach
    void setUp() {
        dealFactProvider = new DealFactProvider();
        facts = new Facts();
        deal = new Deal();
        deal.setPricingModel(new PricingModel());
    }

    @Test
    void testSupportedContextType() {
        assertEquals(Deal.class, dealFactProvider.getSupportedContextType());
    }

    @Test
    void testProvideFactsWithValidDeal() {
        // Setup
        PricingModel pricingModel = deal.getPricingModel();
        pricingModel.setCommercialCreditPercentage(new BigDecimal("30.5"));
        pricingModel.setAllCreditPercentage(new BigDecimal("15.0"));
        pricingModel.setDebitPercentage(new BigDecimal("40.0"));
        pricingModel.setDurbinRegulatedPercentage(new BigDecimal("10.0"));
        pricingModel.setAveragePayment(new BigDecimal("150.00"));

        // Execute
        dealFactProvider.provideFacts(deal, facts);

        // Verify
        assertEquals(deal, facts.get("deal"));
        assertEquals(new BigDecimal("30.5"), facts.get("commercialCreditPercentage"));
        assertEquals(new BigDecimal("15.0"), facts.get("allCreditPercentage"));
        assertEquals(new BigDecimal("40.0"), facts.get("debitPercentage"));
        assertEquals(new BigDecimal("10.0"), facts.get("durbinRegPercentage"));
        assertEquals(new BigDecimal("150.00"), facts.get("averagePayment"));
        assertEquals(false, facts.get("rulesApplied"));
    }

    @Test
    void testProvideFactsWithNullValues() {
        // Execute (PricingModel fields are null by default)
        dealFactProvider.provideFacts(deal, facts);

        // Verify (should default to ZERO as per implementation plan)
        assertEquals(BigDecimal.ZERO, facts.get("commercialCreditPercentage"));
        assertEquals(BigDecimal.ZERO, facts.get("allCreditPercentage"));
        assertEquals(BigDecimal.ZERO, facts.get("debitPercentage"));
        assertEquals(BigDecimal.ZERO, facts.get("durbinRegPercentage"));

        // For averagePayment, if implementation handles nulls by returning ZERO
        assertEquals(BigDecimal.ZERO, facts.get("averagePayment"));
    }

    @Test
    void testProvideFactsWithNullPricingModel() {
        // Setup
        deal.setPricingModel(null);

        // Execute
        dealFactProvider.provideFacts(deal, facts);

        // Verify
        assertNotNull(facts.get("deal"));
        assertNull(facts.get("commercialCreditPercentage"));
    }

    @Test
    void testProvideFactsWithInvalidContext() {
        assertThrows(IllegalArgumentException.class, () -> {
            dealFactProvider.provideFacts(new Object(), facts);
        });
    }
}
