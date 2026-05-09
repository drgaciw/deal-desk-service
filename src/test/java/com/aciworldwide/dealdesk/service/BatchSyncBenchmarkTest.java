package com.aciworldwide.dealdesk.service;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.repository.DealRepository;
import com.aciworldwide.dealdesk.service.impl.DealServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchSyncBenchmarkTest {

    @Mock
    private DealRepository dealRepository;

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
    void benchmarkBatchSyncWithSalesforce() {
        int dealCount = 100;
        List<String> ids = new ArrayList<>();
        List<Deal> deals = new ArrayList<>();

        for (int i = 0; i < dealCount; i++) {
            String id = UUID.randomUUID().toString();
            ids.add(id);
            Deal deal = new Deal();
            deal.setId(id);
            deals.add(deal);
        }

        when(dealRepository.findAllById(ids)).thenReturn(deals);

        // Simulate 10ms latency per single call
        lenient().when(salesforceService.syncDealToOpportunity(any(Deal.class))).thenAnswer((Answer<Deal>) invocation -> {
            try { Thread.sleep(10); } catch (InterruptedException e) {}
            return (Deal) invocation.getArgument(0);
        });

        // Simulate 10ms latency for the entire batch call
        lenient().doAnswer(invocation -> {
            try { Thread.sleep(10); } catch (InterruptedException e) {}
            return null;
        }).when(salesforceService).batchUpdateOpportunities(anyList());

        System.out.println("Starting benchmark for " + dealCount + " deals...");
        long startTime = System.currentTimeMillis();

        dealService.batchSyncWithSalesforce(ids);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("batchSyncWithSalesforce took: " + duration + " ms");
    }
}
