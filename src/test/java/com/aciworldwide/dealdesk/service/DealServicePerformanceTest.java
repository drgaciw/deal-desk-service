package com.aciworldwide.dealdesk.service;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.repository.DealRepository;
import com.aciworldwide.dealdesk.service.impl.DealServiceImpl;
import com.aciworldwide.dealdesk.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DealServicePerformanceTest {

    @Mock
    private DealRepository dealRepository;

    // We need to mock other dependencies of DealServiceImpl even if unused in this method
    @Mock
    private SalesforceService salesforceService;
    @Mock
    private com.aciworldwide.dealdesk.rules.engine.PricingRuleEngine pricingRuleEngine;
    @Mock
    private com.aciworldwide.dealdesk.rules.service.TCVRuleExecutorService tcvRuleExecutorService;
    @Mock
    private com.aciworldwide.dealdesk.rules.service.DealValidationRuleExecutorService dealValidationRuleExecutorService;
    @Mock
    private com.aciworldwide.dealdesk.rules.service.DealStatusRuleExecutorService dealStatusRuleExecutorService;

    @InjectMocks
    private DealServiceImpl dealService;

    @Test
    void benchmarkCalculateTotalValue_InMemory() {
        int dealCount = 100000; // Large number to make CPU visible
        System.out.println("Generating " + dealCount + " deals in memory...");
        List<Deal> deals = TestDataFactory.createDealsWithStatus(DealStatus.APPROVED, dealCount);
        // Set value to 100
        BigDecimal val = new BigDecimal("100.00");
        for (Deal d : deals) {
            d.setValue(val);
        }

        when(dealRepository.calculateTotalValueByStatus(DealStatus.APPROVED))
            .thenReturn(new com.aciworldwide.dealdesk.repository.TotalValueResult(null, new BigDecimal("100.00").multiply(new BigDecimal(dealCount))));
        System.out.println("Generation complete.");

        // Warmup
        dealService.calculateTotalValue(DealStatus.APPROVED);

        long startTime = System.currentTimeMillis();
        BigDecimal totalValue = dealService.calculateTotalValue(DealStatus.APPROVED);
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        System.out.println("calculateTotalValue (Database Aggregation) took: " + duration + " ms for " + dealCount + " items");

        assertThat(totalValue).isEqualByComparingTo(new BigDecimal("100.00").multiply(new BigDecimal(dealCount)));
    }

    @Test
    void benchmarkFindExpiredDeals_Optimized() {
        int totalDeals = 500000;
        int expiredDealsCount = 100;
        ZonedDateTime expirationDate = ZonedDateTime.now().minusDays(30);

        System.out.println("Generating " + expiredDealsCount + " expired deals for benchmark...");

        // In the optimized version, the DB (mock) only returns the matching deals.
        // We don't need to generate the 500k non-matching deals because the service won't see them.
        List<Deal> expiredDeals = new ArrayList<>(expiredDealsCount);

        for (int i = 0; i < expiredDealsCount; i++) {
            Deal d = TestDataFactory.createDeal();
            d.setStatus(DealStatus.SUBMITTED);
            d.setUpdatedAt(expirationDate.minusDays(1)); // Expired
            expiredDeals.add(d);
        }

        System.out.println("Generation complete.");

        when(dealRepository.findByStatusAndUpdatedAtBefore(eq(DealStatus.SUBMITTED), eq(expirationDate)))
                .thenReturn(expiredDeals);

        // Warmup
        dealService.findExpiredDeals(expirationDate);

        // Measure
        long startTime = System.currentTimeMillis();
        List<Deal> result = dealService.findExpiredDeals(expirationDate);
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        System.out.println("findExpiredDeals (Optimized) took: " + duration + " ms (simulating " + totalDeals + " total items in DB)");

        assertThat(result).hasSize(expiredDealsCount);
    }
}
