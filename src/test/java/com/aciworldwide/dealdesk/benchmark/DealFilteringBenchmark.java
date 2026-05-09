package com.aciworldwide.dealdesk.benchmark;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.repository.DealRepository;
import com.aciworldwide.dealdesk.service.impl.DealServiceImpl;
import com.aciworldwide.dealdesk.util.TestDataFactory;
import com.aciworldwide.dealdesk.service.SalesforceService;
import com.aciworldwide.dealdesk.rules.engine.PricingRuleEngine;
import com.aciworldwide.dealdesk.rules.service.TCVRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.DealValidationRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.DealStatusRuleExecutorService;

@ExtendWith(MockitoExtension.class)
public class DealFilteringBenchmark {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private SalesforceService salesforceService;
    @Mock
    private PricingRuleEngine pricingRuleEngine;
    @Mock
    private TCVRuleExecutorService tcvRuleExecutorService;
    @Mock
    private DealValidationRuleExecutorService dealValidationRuleExecutorService;
    @Mock
    private DealStatusRuleExecutorService dealStatusRuleExecutorService;

    @InjectMocks
    private DealServiceImpl dealService;

    @Test
    public void benchmarkFindExpiredDeals() {
        int totalDeals = 100000; // Simulated total in DB
        ZonedDateTime expirationDate = ZonedDateTime.now();
        Random random = new Random();

        // Create only the filtered deals to simulate DB return
        List<Deal> filteredDeals = new ArrayList<>();
        // In previous run, ~5% were matches. Let's create ~5000 matches.
        for (int i = 0; i < 5000; i++) {
            Deal deal = TestDataFactory.createDeal();
            deal.setStatus(DealStatus.SUBMITTED);
            deal.setUpdatedAt(expirationDate.minusDays(random.nextInt(10) + 1));
            filteredDeals.add(deal);
        }

        // Setup mock for new implementation
        when(dealRepository.findByStatusAndUpdatedAtBefore(eq(DealStatus.SUBMITTED), eq(expirationDate)))
            .thenReturn(filteredDeals);

        // Run benchmark
        long startTime = System.nanoTime();
        List<Deal> result = dealService.findExpiredDeals(expirationDate);
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;

        System.out.println("--------------------------------------------------");
        System.out.println("Benchmark Results (Optimized)");
        System.out.println("Simulated Total Deals in DB: " + totalDeals);
        System.out.println("Returned Expired Deals: " + result.size());
        System.out.println("Time taken: " + durationMs + " ms");
        System.out.println("--------------------------------------------------");

        // Verify correctness
        verify(dealRepository).findByStatusAndUpdatedAtBefore(eq(DealStatus.SUBMITTED), eq(expirationDate));
        verify(dealRepository, times(0)).findAll();
    }
}
