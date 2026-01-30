package com.aciworldwide.dealdesk.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.repository.DealRepository;
import com.aciworldwide.dealdesk.service.impl.DealServiceImpl;
import com.aciworldwide.dealdesk.util.TestDataFactory;
import com.aciworldwide.dealdesk.rules.service.DealStatusRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.DealValidationRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.TCVRuleExecutorService;
import com.aciworldwide.dealdesk.rules.engine.PricingRuleEngine;

@ExtendWith(MockitoExtension.class)
class DealServiceBatchSyncTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private SalesforceService salesforceService;

    @Mock
    private DealValidationRuleExecutorService dealValidationRuleExecutorService;

    @Mock
    private DealStatusRuleExecutorService dealStatusRuleExecutorService;

    @Mock
    private TCVRuleExecutorService tcvRuleExecutorService;

    @Mock
    private PricingRuleEngine pricingRuleEngine;

    @InjectMocks
    private DealServiceImpl dealService;

    @Test
    void batchSyncWithSalesforce_CallsBatchUpdate_OptimizedBehavior() {
        // Given
        int dealCount = 5;
        List<Deal> deals = TestDataFactory.createDeals(dealCount);
        List<String> ids = deals.stream().map(Deal::getId).collect(Collectors.toList());

        when(dealRepository.findAllById(ids)).thenReturn(deals);

        // When
        dealService.batchSyncWithSalesforce(ids);

        // Then - Verify optimized behavior
        verify(salesforceService, times(0)).syncDealToOpportunity(any(Deal.class));
        verify(salesforceService, times(1)).batchUpdateOpportunities(deals);
    }
}
