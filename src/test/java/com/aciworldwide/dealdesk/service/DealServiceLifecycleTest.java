package com.aciworldwide.dealdesk.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aciworldwide.dealdesk.exception.DealNotFoundException;
import com.aciworldwide.dealdesk.exception.InvalidDealStateException;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.repository.DealRepository;
import com.aciworldwide.dealdesk.rules.engine.PricingRuleEngine;
import com.aciworldwide.dealdesk.rules.service.DealStatusRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.DealValidationRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.TCVRuleExecutorService;
import com.aciworldwide.dealdesk.service.impl.DealServiceImpl;
import com.aciworldwide.dealdesk.util.TestDataFactory;

@ExtendWith(MockitoExtension.class)
@DisplayName("Deal Lifecycle State Transition Tests")
class DealServiceLifecycleTest {

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

    private Deal draftDeal;

    @BeforeEach
    void setUp() {
        draftDeal = TestDataFactory.createDealWithStatus(DealStatus.DRAFT);
        draftDeal.setId("deal-001");
    }

    // -------------------------------------------------------------------------
    // DRAFT → SUBMITTED
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DRAFT → SUBMITTED transitions")
    class DraftToSubmitted {

        @Test
        @DisplayName("submitForApproval sets status to SUBMITTED and persists")
        void submitForApproval_FromDraft_SetsSubmittedStatus() {
            when(dealRepository.findById("deal-001")).thenReturn(Optional.of(draftDeal));
            Deal saved = TestDataFactory.createDealWithStatus(DealStatus.SUBMITTED);
            saved.setId("deal-001");
            when(dealRepository.save(any(Deal.class))).thenReturn(saved);

            Deal result = dealService.submitForApproval("deal-001");

            assertThat(result.getStatus()).isEqualTo(DealStatus.SUBMITTED);
            verify(dealStatusRuleExecutorService).executeDealStatusRules(any(Deal.class));
            verify(dealRepository).save(any(Deal.class));
        }

        @Test
        @DisplayName("submitForApproval from SUBMITTED state throws InvalidDealStateException")
        void submitForApproval_FromSubmitted_Throws() {
            Deal submittedDeal = TestDataFactory.createDealWithStatus(DealStatus.SUBMITTED);
            submittedDeal.setId("deal-001");
            when(dealRepository.findById("deal-001")).thenReturn(Optional.of(submittedDeal));

            assertThatThrownBy(() -> dealService.submitForApproval("deal-001"))
                    .isInstanceOf(InvalidDealStateException.class)
                    .hasMessageContaining("SUBMITTED");
        }

        @Test
        @DisplayName("submitForApproval from APPROVED state throws InvalidDealStateException")
        void submitForApproval_FromApproved_Throws() {
            Deal approvedDeal = TestDataFactory.createDealWithStatus(DealStatus.APPROVED);
            approvedDeal.setId("deal-001");
            when(dealRepository.findById("deal-001")).thenReturn(Optional.of(approvedDeal));

            assertThatThrownBy(() -> dealService.submitForApproval("deal-001"))
                    .isInstanceOf(InvalidDealStateException.class);
        }

        @Test
        @DisplayName("submitForApproval for non-existent deal throws DealNotFoundException")
        void submitForApproval_NotFound_Throws() {
            when(dealRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dealService.submitForApproval("missing"))
                    .isInstanceOf(DealNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // SUBMITTED → APPROVED
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("SUBMITTED → APPROVED transitions")
    class SubmittedToApproved {

        private Deal submittedDeal;

        @BeforeEach
        void setUp() {
            submittedDeal = TestDataFactory.createDealWithStatus(DealStatus.SUBMITTED);
            submittedDeal.setId("deal-001");
        }

        @Test
        @DisplayName("approveDeal sets status to APPROVED and records approver")
        void approveDeal_FromSubmitted_SetsApprovedStatus() {
            when(dealRepository.findById("deal-001")).thenReturn(Optional.of(submittedDeal));
            when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
            when(salesforceService.syncDealToOpportunity(any(Deal.class))).thenReturn(submittedDeal);

            Deal result = dealService.approveDeal("deal-001", "approver-user");

            assertThat(result.getStatus()).isEqualTo(DealStatus.APPROVED);
            assertThat(result.getApprovedBy()).isEqualTo("approver-user");
            assertThat(result.getApprovedAt()).isNotNull();
            verify(salesforceService).syncDealToOpportunity(any(Deal.class));
            verify(dealStatusRuleExecutorService).executeDealStatusRules(any(Deal.class));
        }

        @Test
        @DisplayName("approveDeal from DRAFT state throws InvalidDealStateException")
        void approveDeal_FromDraft_Throws() {
            when(dealRepository.findById("deal-001")).thenReturn(Optional.of(draftDeal));

            assertThatThrownBy(() -> dealService.approveDeal("deal-001", "approver-user"))
                    .isInstanceOf(InvalidDealStateException.class);
            verify(dealRepository, never()).save(any());
        }

        @Test
        @DisplayName("approveDeal from APPROVED state throws InvalidDealStateException")
        void approveDeal_FromApproved_Throws() {
            Deal approvedDeal = TestDataFactory.createDealWithStatus(DealStatus.APPROVED);
            approvedDeal.setId("deal-001");
            when(dealRepository.findById("deal-001")).thenReturn(Optional.of(approvedDeal));

            assertThatThrownBy(() -> dealService.approveDeal("deal-001", "approver-user"))
                    .isInstanceOf(InvalidDealStateException.class);
        }
    }

    // -------------------------------------------------------------------------
    // SUBMITTED → REJECTED
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("SUBMITTED → REJECTED transitions")
    class SubmittedToRejected {

        private Deal submittedDeal;

        @BeforeEach
        void setUp() {
            submittedDeal = TestDataFactory.createDealWithStatus(DealStatus.SUBMITTED);
            submittedDeal.setId("deal-001");
        }

        @Test
        @DisplayName("rejectDeal sets status to REJECTED and saves")
        void rejectDeal_FromSubmitted_SetsRejectedStatus() {
            when(dealRepository.findById("deal-001")).thenReturn(Optional.of(submittedDeal));
            when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));

            Deal result = dealService.rejectDeal("deal-001", "approver-user", "Price too high");

            assertThat(result.getStatus()).isEqualTo(DealStatus.REJECTED);
            verify(dealStatusRuleExecutorService).executeDealStatusRules(any(Deal.class));
            verify(dealRepository).save(any(Deal.class));
        }

        @Test
        @DisplayName("rejectDeal for missing deal throws DealNotFoundException")
        void rejectDeal_NotFound_Throws() {
            when(dealRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dealService.rejectDeal("missing", "user", "reason"))
                    .isInstanceOf(DealNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // Any state → CANCELLED
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Cancellation transitions")
    class CancelDeal {

        @Test
        @DisplayName("cancelDeal from DRAFT sets status to CANCELLED")
        void cancelDeal_FromDraft_SetsCancelledStatus() {
            when(dealRepository.findById("deal-001")).thenReturn(Optional.of(draftDeal));
            when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));

            Deal result = dealService.cancelDeal("deal-001", "Withdrawn by customer");

            assertThat(result.getStatus()).isEqualTo(DealStatus.CANCELLED);
            verify(dealRepository).save(any(Deal.class));
        }

        @Test
        @DisplayName("cancelDeal from SUBMITTED sets status to CANCELLED")
        void cancelDeal_FromSubmitted_SetsCancelledStatus() {
            Deal submittedDeal = TestDataFactory.createDealWithStatus(DealStatus.SUBMITTED);
            submittedDeal.setId("deal-001");
            when(dealRepository.findById("deal-001")).thenReturn(Optional.of(submittedDeal));
            when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));

            Deal result = dealService.cancelDeal("deal-001", "Cancelled");

            assertThat(result.getStatus()).isEqualTo(DealStatus.CANCELLED);
        }

        @Test
        @DisplayName("cancelDeal for missing deal throws DealNotFoundException")
        void cancelDeal_NotFound_Throws() {
            when(dealRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dealService.cancelDeal("missing", "reason"))
                    .isInstanceOf(DealNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // Delete (only DRAFT)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Delete transitions")
    class DeleteDeal {

        @Test
        @DisplayName("deleteDeal on DRAFT deal succeeds")
        void deleteDeal_FromDraft_Succeeds() {
            when(dealRepository.findById("deal-001")).thenReturn(Optional.of(draftDeal));

            assertThatCode(() -> dealService.deleteDeal("deal-001")).doesNotThrowAnyException();

            verify(dealRepository).delete(draftDeal);
        }

        @Test
        @DisplayName("deleteDeal on SUBMITTED deal throws InvalidDealStateException")
        void deleteDeal_FromSubmitted_Throws() {
            Deal submittedDeal = TestDataFactory.createDealWithStatus(DealStatus.SUBMITTED);
            submittedDeal.setId("deal-001");
            when(dealRepository.findById("deal-001")).thenReturn(Optional.of(submittedDeal));

            assertThatThrownBy(() -> dealService.deleteDeal("deal-001"))
                    .isInstanceOf(InvalidDealStateException.class);
            verify(dealRepository, never()).delete(any(Deal.class));
        }

        @Test
        @DisplayName("deleteDeal on APPROVED deal throws InvalidDealStateException")
        void deleteDeal_FromApproved_Throws() {
            Deal approvedDeal = TestDataFactory.createDealWithStatus(DealStatus.APPROVED);
            approvedDeal.setId("deal-001");
            when(dealRepository.findById("deal-001")).thenReturn(Optional.of(approvedDeal));

            assertThatThrownBy(() -> dealService.deleteDeal("deal-001"))
                    .isInstanceOf(InvalidDealStateException.class);
        }
    }

    // -------------------------------------------------------------------------
    // Batch status update
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Batch operations")
    class BatchOperations {

        @Test
        @DisplayName("batchUpdateStatus updates all deals and returns them")
        void batchUpdateStatus_UpdatesAllDeals() {
            List<Deal> deals = TestDataFactory.createDealsWithStatus(DealStatus.DRAFT, 3);
            List<String> ids = List.of("id-1", "id-2", "id-3");
            when(dealRepository.findAllById(ids)).thenReturn(deals);
            when(dealRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            List<Deal> results = dealService.batchUpdateStatus(ids, DealStatus.CANCELLED);

            assertThat(results).hasSize(3);
            assertThat(results).allMatch(d -> d.getStatus() == DealStatus.CANCELLED);
        }

        @Test
        @DisplayName("batchSyncWithSalesforce delegates to salesforceService")
        void batchSyncWithSalesforce_DelegatesToService() {
            List<Deal> deals = TestDataFactory.createDeals(2);
            List<String> ids = List.of("id-1", "id-2");
            when(dealRepository.findAllById(ids)).thenReturn(deals);

            dealService.batchSyncWithSalesforce(ids);

            verify(salesforceService).batchUpdateOpportunities(deals);
        }
    }

    // -------------------------------------------------------------------------
    // Update deal
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Update deal")
    class UpdateDeal {

        @Test
        @DisplayName("updateDeal with valid data returns updated deal")
        void updateDeal_ValidData_ReturnsUpdatedDeal() {
            Deal updateData = TestDataFactory.createDeal();
            updateData.setValue(new BigDecimal("200000.00"));
            when(dealRepository.findById("deal-001")).thenReturn(Optional.of(draftDeal));
            when(dealValidationRuleExecutorService.executeValidationRules(any())).thenReturn(List.of());
            when(dealRepository.save(any(Deal.class))).thenAnswer(inv -> inv.getArgument(0));
            when(salesforceService.syncDealToOpportunity(any(Deal.class))).thenReturn(draftDeal);

            Deal result = dealService.updateDeal("deal-001", updateData);

            assertThat(result).isNotNull();
            verify(dealRepository).save(any(Deal.class));
        }

        @Test
        @DisplayName("updateDeal with null deal throws IllegalArgumentException")
        void updateDeal_NullDeal_Throws() {
            assertThatThrownBy(() -> dealService.updateDeal("deal-001", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("updateDeal with missing deal throws DealNotFoundException")
        void updateDeal_NotFound_Throws() {
            Deal updateData = TestDataFactory.createDeal();
            when(dealRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dealService.updateDeal("missing", updateData))
                    .isInstanceOf(DealNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // syncWithSalesforce
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Salesforce sync")
    class SalesforceSync {

        @Test
        @DisplayName("syncWithSalesforce fetches deal, syncs and evaluates pricing rules")
        void syncWithSalesforce_ValidDeal_SyncsAndEvaluatesRules() {
            when(dealRepository.findById("deal-001")).thenReturn(Optional.of(draftDeal));
            when(salesforceService.syncDealToOpportunity(any(Deal.class))).thenReturn(draftDeal);
            when(dealRepository.save(any(Deal.class))).thenReturn(draftDeal);

            Deal result = dealService.syncWithSalesforce("deal-001");

            assertThat(result).isNotNull();
            verify(pricingRuleEngine).evaluateRules(any(Deal.class));
            verify(dealRepository).save(any(Deal.class));
        }
    }

    // -------------------------------------------------------------------------
    // getRecentDeals and findExpiredDeals
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Query methods")
    class QueryMethods {

        @Test
        @DisplayName("getRecentDeals delegates to repository with correct params")
        void getRecentDeals_ReturnsExpectedDeals() {
            ZonedDateTime since = ZonedDateTime.now().minusDays(7);
            List<DealStatus> statuses = List.of(DealStatus.DRAFT, DealStatus.SUBMITTED);
            List<Deal> expected = TestDataFactory.createDeals(2);
            when(dealRepository.findRecentDeals(since, statuses)).thenReturn(expected);

            List<Deal> result = dealService.getRecentDeals(since, statuses);

            assertThat(result).hasSize(2);
            verify(dealRepository).findRecentDeals(since, statuses);
        }

        @Test
        @DisplayName("countDealsByStatus delegates to repository")
        void countDealsByStatus_ReturnsCount() {
            when(dealRepository.countByStatus(DealStatus.DRAFT)).thenReturn(5L);

            long count = dealService.countDealsByStatus(DealStatus.DRAFT);

            assertThat(count).isEqualTo(5L);
        }
    }
}
