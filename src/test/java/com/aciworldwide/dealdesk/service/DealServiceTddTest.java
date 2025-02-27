package com.aciworldwide.dealdesk.service;

import com.aciworldwide.dealdesk.exception.SalesforceIntegrationException;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.repository.DealRepository;
import com.aciworldwide.dealdesk.service.impl.DealServiceImpl;
import com.aciworldwide.dealdesk.rules.service.DealValidationRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.TCVRuleExecutorService;
import com.aciworldwide.dealdesk.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;

/**
 * Unit tests for {@link DealServiceImpl} focusing on deal creation and validation.
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("DealService Test Suite")
class DealServiceTddTest {

    private static final String SALESFORCE_ERROR = "Invalid Salesforce opportunity ID";
    private static final String VALIDATION_ERROR = "Validation rule failed";
    private static final String DUPLICATE_DEAL_ERROR = "Deal with this Salesforce opportunity ID already exists";
    private static final String NULL_DEAL_ERROR = "Deal cannot be null";

    @Mock
    private DealRepository dealRepository;

    @Mock
    private SalesforceService salesforceService;

    @Mock
    private DealValidationRuleExecutorService dealValidationRuleExecutorService;

    @Mock
    private TCVRuleExecutorService tcvRuleExecutorService;

    @InjectMocks
    private DealServiceImpl dealService;

    private Deal testDeal;

    @BeforeEach
    void setUp() {
        testDeal = TestDataFactory.createDeal();
    }

    @Nested
    @DisplayName("Deal Creation Success Tests")
    class DealCreationSuccessTests {
        
        @Test
        @DisplayName("Should successfully create valid deal")
        void createDeal_ValidDeal_ReturnsSavedDeal() {
            // Given
            when(dealRepository.existsBySalesforceOpportunityId(any())).thenReturn(false);
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);
            doNothing().when(salesforceService).syncDealToOpportunity(any(Deal.class));

            // When
            Deal result = dealService.createDeal(testDeal);

            // Then
            assertThat(result)
                .as("Created deal should not be null")
                .isNotNull();
            assertThat(result.getStatus())
                .as("New deal should have DRAFT status")
                .isEqualTo(DealStatus.DRAFT);
            
            verify(dealRepository).save(any(Deal.class));
            verify(salesforceService).syncDealToOpportunity(any(Deal.class));
        }

        @Test
        @DisplayName("Should validate rules in correct order before saving")
        void createDeal_ValidatesRulesBeforeSaving() {
            // Given
            when(dealRepository.existsBySalesforceOpportunityId(any())).thenReturn(false);
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);
            doNothing().when(salesforceService).syncDealToOpportunity(any(Deal.class));
            doNothing().when(dealValidationRuleExecutorService).executeValidationRules(any(Deal.class));

            // When
            Deal result = dealService.createDeal(testDeal);

            // Then
            assertThat(result).isNotNull();
            
            InOrder order = inOrder(dealValidationRuleExecutorService, tcvRuleExecutorService,
                                  dealRepository, salesforceService);
            order.verify(dealValidationRuleExecutorService).executeValidationRules(testDeal);
            order.verify(tcvRuleExecutorService).executeTCVRules(testDeal);
            order.verify(dealRepository).save(testDeal);
            order.verify(salesforceService).syncDealToOpportunity(testDeal);
        }
    }

    @Nested
    @DisplayName("Deal Creation Validation Tests")
    class DealCreationValidationTests {

        @Test
        @DisplayName("Should reject null deal")
        void createDeal_NullDeal_ThrowsException() {
            assertThatThrownBy(() -> dealService.createDeal(null))
                .as("Should throw IllegalArgumentException for null deal")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(NULL_DEAL_ERROR);
        }

        @Test
        @DisplayName("Should reject duplicate Salesforce opportunity ID")
        void createDeal_DuplicateOpportunityId_ThrowsException() {
            // Given
            when(dealRepository.existsBySalesforceOpportunityId(any())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> dealService.createDeal(testDeal))
                .as("Should throw IllegalArgumentException for duplicate opportunity ID")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(DUPLICATE_DEAL_ERROR);
            
            verify(dealRepository, never()).save(any());
            verify(salesforceService, never()).syncDealToOpportunity(any());
        }

        @ParameterizedTest(name = "Should reject invalid deal: {0}")
        @MethodSource("com.aciworldwide.dealdesk.service.DealServiceTddTest#invalidDealData")
        void createDeal_InvalidDeal_ThrowsException(String testCase, Deal invalidDeal) {
            assertThatThrownBy(() -> dealService.createDeal(invalidDeal))
                .as("Should throw IllegalArgumentException for " + testCase)
                .isInstanceOf(IllegalArgumentException.class);
            
            verify(dealRepository, never()).save(any());
            verify(salesforceService, never()).syncDealToOpportunity(any());
        }
    }

    @Nested
    @DisplayName("Salesforce Integration Tests")
    class SalesforceIntegrationTests {

        @Test
        @DisplayName("Should handle Salesforce sync failure")
        void createDeal_SalesforceSyncFailure_ThrowsException() {
            // Given
            when(dealRepository.existsBySalesforceOpportunityId(any())).thenReturn(false);
            when(salesforceService.validateOpportunityExists(any())).thenReturn(true);
            doThrow(new SalesforceIntegrationException(SALESFORCE_ERROR))
                .when(salesforceService).syncDealToOpportunity(any(Deal.class));

            // When/Then
            assertThatThrownBy(() -> dealService.createDeal(testDeal))
                .as("Should throw SalesforceIntegrationException for sync failure")
                .isInstanceOf(SalesforceIntegrationException.class)
                .hasMessageContaining(SALESFORCE_ERROR);
            
            verify(dealRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Rule Execution Tests")
    class RuleExecutionTests {

        @Test
        @DisplayName("Should handle validation rule failure")
        void createDeal_ValidationRuleFails_ThrowsException() {
            // Given
            doThrow(new IllegalArgumentException(VALIDATION_ERROR))
                .when(dealValidationRuleExecutorService).executeValidationRules(any(Deal.class));

            // When/Then
            assertThatThrownBy(() -> dealService.createDeal(testDeal))
                .as("Should throw IllegalArgumentException for validation rule failure")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(VALIDATION_ERROR);
            
            verify(dealRepository, never()).save(any());
        }
    }
}