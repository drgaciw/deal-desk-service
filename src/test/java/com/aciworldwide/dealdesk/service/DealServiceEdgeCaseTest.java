package com.aciworldwide.dealdesk.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aciworldwide.dealdesk.exception.DealNotFoundException;
import com.aciworldwide.dealdesk.exception.InvalidDealStateException;
import com.aciworldwide.dealdesk.exception.SalesforceIntegrationException;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.repository.DealRepository;
import com.aciworldwide.dealdesk.rules.service.DealValidationRuleExecutorService;
import com.aciworldwide.dealdesk.service.impl.DealServiceImpl;
import com.aciworldwide.dealdesk.util.TestDataFactory;

/**
 * Tests edge cases and error scenarios for the Deal Service implementation.
 * Focuses on invalid state transitions, error handling, and integration failures.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Deal Service Edge Cases")
class DealServiceEdgeCaseTest {

    private static final String TEST_ID = "1";
    private static final String TEST_APPROVER = "approver-1";
    private static final String TEST_REJECTOR = "rejector-1";
    private static final String TEST_REASON = "test reason";
    private static final String SF_ERROR_MESSAGE = "API limit reached";

    @Mock
    private DealRepository dealRepository;

    @Mock
    private SalesforceService salesforceService;

    @Mock
    private DealValidationRuleExecutorService dealValidationRuleExecutorService;

    @InjectMocks
    private DealServiceImpl dealService;

    private Deal testDeal;

    @BeforeEach
    void setUp() {
        testDeal = TestDataFactory.createDeal();
    }

    @Nested
    @DisplayName("Salesforce Integration Edge Cases")
    class SalesforceIntegrationTests {
        
        @Test
        @DisplayName("Should throw exception when Salesforce sync fails during creation")
        void createDeal_WhenSalesforceSyncFails_ThrowsException() {
            // Arrange
            when(dealRepository.existsBySalesforceOpportunityId(anyString())).thenReturn(false);
            when(salesforceService.validateOpportunityExists(anyString())).thenReturn(true);
            when(salesforceService.syncDealToOpportunity(any(Deal.class)))
                .thenThrow(new SalesforceIntegrationException(SF_ERROR_MESSAGE));

            // Act & Assert
            assertThatThrownBy(() -> dealService.createDeal(testDeal))
                .isInstanceOf(SalesforceIntegrationException.class)
                .hasMessageContaining(SF_ERROR_MESSAGE)
                .satisfies(exception -> {
                    verify(dealRepository, never()).save(any());
                    verify(salesforceService).syncDealToOpportunity(testDeal);
                });
        }

        @Test
        @DisplayName("Should throw exception when opportunity validation fails")
        void createDeal_WhenOpportunityValidationFails_ThrowsException() {
            // Arrange
            when(dealRepository.existsBySalesforceOpportunityId(anyString())).thenReturn(false);
            when(salesforceService.validateOpportunityExists(anyString())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> dealService.createDeal(testDeal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Salesforce opportunity")
                .satisfies(exception -> {
                    verify(dealRepository, never()).save(any());
                    verify(salesforceService, never()).syncDealToOpportunity(any());
                });
        }
    }

    @Nested
    @DisplayName("Invalid State Transitions")
    class InvalidStateTransitionTests {

        @ParameterizedTest(name = "From {0} status")
        @MethodSource("invalidSubmitStatuses")
        @DisplayName("Should throw exception when submitting deal from invalid status")
        void submitForApproval_FromInvalidStatus_ThrowsException(DealStatus status) {
            // Arrange
            testDeal.setStatus(status);
            when(dealRepository.findById(TEST_ID)).thenReturn(Optional.of(testDeal));

            // Act & Assert
            assertThatThrownBy(() -> dealService.submitForApproval(TEST_ID))
                .isInstanceOf(InvalidDealStateException.class)
                .hasMessageContaining("Cannot submit deal from status: " + status);
        }

        private static Stream<Arguments> invalidSubmitStatuses() {
            return Stream.of(
                Arguments.of(DealStatus.SUBMITTED),
                Arguments.of(DealStatus.APPROVED),
                Arguments.of(DealStatus.REJECTED),
                Arguments.of(DealStatus.CLOSED_WON)
            );
        }

        @ParameterizedTest(name = "From {0} status")
        @MethodSource("invalidApprovalStatuses")
        @DisplayName("Should throw exception when approving deal from invalid status")
        void approveDeal_FromInvalidStatus_ThrowsException(DealStatus status) {
            // Arrange
            testDeal.setStatus(status);
            when(dealRepository.findById(TEST_ID)).thenReturn(Optional.of(testDeal));

            // Act & Assert
            assertThatThrownBy(() -> dealService.approveDeal(TEST_ID, TEST_APPROVER))
                .isInstanceOf(InvalidDealStateException.class)
                .hasMessageContaining("Cannot approve deal from status: " + status);
        }

        private static Stream<Arguments> invalidApprovalStatuses() {
            return Stream.of(
                Arguments.of(DealStatus.DRAFT),
                Arguments.of(DealStatus.APPROVED),
                Arguments.of(DealStatus.REJECTED),
                Arguments.of(DealStatus.CLOSED_WON)
            );
        }

        @Test
        @DisplayName("Should throw exception when deleting non-draft deal")
        void deleteDeal_WhenNotInDraftStatus_ThrowsException() {
            // Arrange
            testDeal.setStatus(DealStatus.SUBMITTED);
            when(dealRepository.findById(TEST_ID)).thenReturn(Optional.of(testDeal));

            // Act & Assert
            assertThatThrownBy(() -> dealService.deleteDeal(TEST_ID))
                .isInstanceOf(InvalidDealStateException.class)
                .hasMessageContaining("Cannot delete deal in status: SUBMITTED")
                .satisfies(exception -> verify(dealRepository, never()).deleteById(any()));
        }
    }

    @Nested
    @DisplayName("Deal State Validation")
    class DealStateValidationTests {

        @ParameterizedTest(name = "In {0} status")
        @MethodSource("nonUpdateableStatuses")
        @DisplayName("Should throw exception when updating deal in invalid status")
        void updateDeal_InInvalidStatus_ThrowsException(DealStatus status) {
            // Arrange
            testDeal.setStatus(status);
            when(dealRepository.findById(TEST_ID)).thenReturn(Optional.of(testDeal));

            // Act & Assert
            assertThatThrownBy(() -> dealService.updateDeal(TEST_ID, testDeal))
                .isInstanceOf(InvalidDealStateException.class)
                .hasMessageContaining("Cannot update deal in status: " + status);
        }

        private static Stream<Arguments> nonUpdateableStatuses() {
            return Stream.of(
                Arguments.of(DealStatus.APPROVED),
                Arguments.of(DealStatus.CLOSED_WON),
                Arguments.of(DealStatus.CLOSED_LOST)
            );
        }

        @Test
        @DisplayName("Should throw exception when rejecting deal from draft status")
        void rejectDeal_FromDraftStatus_ThrowsException() {
            // Arrange
            testDeal.setStatus(DealStatus.DRAFT);
            when(dealRepository.findById(TEST_ID)).thenReturn(Optional.of(testDeal));

            // Act & Assert
            assertThatThrownBy(() -> dealService.rejectDeal(TEST_ID, TEST_REJECTOR, TEST_REASON))
                .isInstanceOf(InvalidDealStateException.class)
                .hasMessageContaining("Cannot reject deal from status: DRAFT");
        }

        @ParameterizedTest(name = "In {0} status")
        @MethodSource("nonCancellableStatuses")
        @DisplayName("Should throw exception when canceling deal in invalid status")
        void cancelDeal_InInvalidStatus_ThrowsException(DealStatus status) {
            // Arrange
            testDeal.setStatus(status);
            when(dealRepository.findById(TEST_ID)).thenReturn(Optional.of(testDeal));

            // Act & Assert
            assertThatThrownBy(() -> dealService.cancelDeal(TEST_ID, TEST_REASON))
                .isInstanceOf(InvalidDealStateException.class)
                .hasMessageContaining("Cannot cancel deal in status: " + status);
        }

        private static Stream<Arguments> nonCancellableStatuses() {
            return Stream.of(
                Arguments.of(DealStatus.CLOSED_WON),
                Arguments.of(DealStatus.CLOSED_LOST),
                Arguments.of(DealStatus.CANCELLED)
            );
        }
    }

    @Nested
    @DisplayName("Not Found Scenarios")
    class NotFoundTests {
        
        @Test
        @DisplayName("Should throw exception when retrieving non-existent deal")
        void getDealById_WhenDealNotFound_ThrowsException() {
            // Arrange
            when(dealRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> dealService.getDealById(TEST_ID))
                .isInstanceOf(DealNotFoundException.class)
                .hasMessageContaining("Deal not found with id: " + TEST_ID);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent deal")
        void updateDeal_WhenDealNotFound_ThrowsException() {
            // Arrange
            when(dealRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> dealService.updateDeal(TEST_ID, testDeal))
                .isInstanceOf(DealNotFoundException.class)
                .hasMessageContaining("Deal not found with id: " + TEST_ID)
                .satisfies(exception -> verify(dealRepository, never()).save(any()));
        }
    }
}