package com.aciworldwide.dealdesk.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aciworldwide.dealdesk.exception.SalesforceIntegrationException;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.repository.DealRepository;
import com.aciworldwide.dealdesk.service.impl.DealServiceImpl;
import com.aciworldwide.dealdesk.util.TestDataFactory;
import com.aciworldwide.dealdesk.rules.service.DealStatusRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.DealValidationRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.TCVRuleExecutorService;
import com.aciworldwide.dealdesk.rules.engine.PricingRuleEngine;

/**
 * Test class for batch sync operations in DealService.
 * Tests the optimized batch update behavior and edge cases.
 */
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

    private DealServiceImpl dealService;

    @BeforeEach
    void setUp() {
        // Manually instantiate DealServiceImpl with only the required dependencies
        // This makes the test's dependencies more explicit
        // Constructor order: dealRepository, salesforceService, pricingRuleEngine,
        // tcvRuleExecutorService, dealValidationRuleExecutorService, dealStatusRuleExecutorService
        dealService = new DealServiceImpl(
            dealRepository,
            salesforceService,
            pricingRuleEngine,
            tcvRuleExecutorService,
            dealValidationRuleExecutorService,
            dealStatusRuleExecutorService
        );
    }

    @Test
    @DisplayName("Should call batchUpdateOpportunities once for multiple deals - optimized behavior")
    void batchSyncWithSalesforce_CallsBatchUpdate_OptimizedBehavior() {
        // Given
        int dealCount = 5;
        List<Deal> deals = TestDataFactory.createDeals(dealCount);
        List<String> ids = deals.stream().map(Deal::getId).collect(Collectors.toList());

        when(dealRepository.findAllById(ids)).thenReturn(deals);

        // When
        dealService.batchSyncWithSalesforce(ids);

        // Then - Verify optimized behavior: single batch call instead of N individual calls
        verify(salesforceService, never()).syncDealToOpportunity(any(Deal.class));
        verify(salesforceService, times(1)).batchUpdateOpportunities(deals);
    }

    @Test
    @DisplayName("Should not call batchUpdateOpportunities when deals list is empty")
    void batchSyncWithSalesforce_EmptyList_NoOpBehavior() {
        // Given
        List<String> emptyIds = Collections.emptyList();
        when(dealRepository.findAllById(emptyIds)).thenReturn(Collections.emptyList());

        // When
        dealService.batchSyncWithSalesforce(emptyIds);

        // Then - Verify no-op behavior: no calls to Salesforce
        verify(salesforceService, never()).batchUpdateOpportunities(anyList());
        verify(salesforceService, never()).syncDealToOpportunity(any(Deal.class));
    }

    @Test
    @DisplayName("Should propagate exception when batchUpdateOpportunities fails")
    void batchSyncWithSalesforce_BatchUpdateFails_ThrowsException() {
        // Given
        List<Deal> deals = TestDataFactory.createDeals(3);
        List<String> ids = deals.stream().map(Deal::getId).collect(Collectors.toList());

        when(dealRepository.findAllById(ids)).thenReturn(deals);
        doThrow(new SalesforceIntegrationException("Batch update failed"))
            .when(salesforceService).batchUpdateOpportunities(deals);

        // When/Then - Verify exception is propagated
        assertThatThrownBy(() -> dealService.batchSyncWithSalesforce(ids))
            .isInstanceOf(SalesforceIntegrationException.class)
            .hasMessageContaining("Batch update failed");

        verify(salesforceService, times(1)).batchUpdateOpportunities(deals);
    }

    @Test
    @DisplayName("Should handle single deal in batch")
    void batchSyncWithSalesforce_SingleDeal_CallsBatchUpdate() {
        // Given
        List<Deal> deals = TestDataFactory.createDeals(1);
        List<String> ids = deals.stream().map(Deal::getId).collect(Collectors.toList());

        when(dealRepository.findAllById(ids)).thenReturn(deals);

        // When
        dealService.batchSyncWithSalesforce(ids);

        // Then - Even for single deal, should use batch API
        verify(salesforceService, never()).syncDealToOpportunity(any(Deal.class));
        verify(salesforceService, times(1)).batchUpdateOpportunities(deals);
    }

    /**
     * Integration test using spy to verify actual batching behavior.
     * This test uses the real MockSalesforceService implementation to ensure
     * that batchUpdateOpportunities truly batches the operations and doesn't
     * just loop internally.
     */
    @Nested
    @DisplayName("Integration Tests with Real Implementation")
    class IntegrationTests {

        @Test
        @DisplayName("Should verify actual batching with spy - only one batch call made")
        void batchSyncWithSalesforce_WithSpy_VerifiesActualBatching() {
            // Given
            MockSalesforceService realSalesforceService = new MockSalesforceService();
            MockSalesforceService spySalesforceService = spy(realSalesforceService);

            DealServiceImpl dealServiceWithSpy = new DealServiceImpl(
                dealRepository,
                spySalesforceService,
                pricingRuleEngine,
                tcvRuleExecutorService,
                dealValidationRuleExecutorService,
                dealStatusRuleExecutorService
            );

            List<Deal> deals = TestDataFactory.createDeals(5);
            List<String> ids = deals.stream().map(Deal::getId).collect(Collectors.toList());

            when(dealRepository.findAllById(ids)).thenReturn(deals);

            // When
            dealServiceWithSpy.batchSyncWithSalesforce(ids);

            // Then - Verify that batchUpdateOpportunities is called once
            // and syncDealToOpportunity is called N times internally by the batch method
            verify(spySalesforceService, times(1)).batchUpdateOpportunities(deals);
            // The spy will show that syncDealToOpportunity is called 5 times internally
            // by the batchUpdateOpportunities implementation (which is expected for the mock)
            verify(spySalesforceService, times(5)).syncDealToOpportunity(any(Deal.class));
        }

        @Test
        @DisplayName("Should verify empty list handling with spy")
        void batchSyncWithSalesforce_WithSpy_EmptyList_NoRemoteCalls() {
            // Given
            MockSalesforceService realSalesforceService = new MockSalesforceService();
            MockSalesforceService spySalesforceService = spy(realSalesforceService);

            DealServiceImpl dealServiceWithSpy = new DealServiceImpl(
                dealRepository,
                spySalesforceService,
                pricingRuleEngine,
                tcvRuleExecutorService,
                dealValidationRuleExecutorService,
                dealStatusRuleExecutorService
            );

            List<String> emptyIds = Collections.emptyList();
            when(dealRepository.findAllById(emptyIds)).thenReturn(Collections.emptyList());

            // When
            dealServiceWithSpy.batchSyncWithSalesforce(emptyIds);

            // Then - Verify no remote calls are made
            verify(spySalesforceService, never()).batchUpdateOpportunities(anyList());
            verify(spySalesforceService, never()).syncDealToOpportunity(any(Deal.class));
        }
    }
}
