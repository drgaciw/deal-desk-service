package com.aciworldwide.dealdesk.service;

import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.repository.DealRepository;
import com.aciworldwide.dealdesk.service.impl.DealServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class DealServiceOptimizationTest {

    @Mock
    private DealRepository dealRepository;

    @Mock private SalesforceService salesforceService;
    @Mock private com.aciworldwide.dealdesk.rules.engine.PricingRuleEngine pricingRuleEngine;
    @Mock private com.aciworldwide.dealdesk.rules.service.TCVRuleExecutorService tcvRuleExecutorService;
    @Mock private com.aciworldwide.dealdesk.rules.service.DealValidationRuleExecutorService dealValidationRuleExecutorService;
    @Mock private com.aciworldwide.dealdesk.rules.service.DealStatusRuleExecutorService dealStatusRuleExecutorService;
    @Mock private com.aciworldwide.dealdesk.metrics.DealMetricsService dealMetricsService;

    @InjectMocks
    private DealServiceImpl dealService;

    @Test
    void countDealsByStatus_shouldUseCountQuery() {
        DealStatus status = DealStatus.DRAFT;

        // When
        dealService.countDealsByStatus(status);

        // Then
        // Verify countByStatus IS called
        verify(dealRepository).countByStatus(status);

        // Verify findByStatus is NOT called
        verify(dealRepository, never()).findByStatus(status);
    }
}
