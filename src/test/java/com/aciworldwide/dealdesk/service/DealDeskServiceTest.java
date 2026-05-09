package com.aciworldwide.dealdesk.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Collections;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aciworldwide.dealdesk.exception.DealNotFoundException;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.model.tcv.TCVCalculation;
import com.aciworldwide.dealdesk.repository.DealRepository;
import com.aciworldwide.dealdesk.service.impl.DealServiceImpl;
import com.aciworldwide.dealdesk.rules.engine.PricingRuleEngine;
import com.aciworldwide.dealdesk.rules.service.TCVRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.DealValidationRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.DealStatusRuleExecutorService;

@ExtendWith(MockitoExtension.class)
@DisplayName("DealDesk Service Tests")
class DealDeskServiceTest {

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

    private Deal testDeal;

    @BeforeEach
    void setUp() {
        testDeal = Deal.builder()
            .id("test-deal-1")
            .name("Test Deal")
            .status(DealStatus.DRAFT)
            .value(BigDecimal.valueOf(100000))
            .createdAt(ZonedDateTime.now())
            .tcvCalculation(TCVCalculation.builder()
                .baseValue(BigDecimal.valueOf(100000))
                .build())
            .build();
    }

    @Nested
    @DisplayName("Deal Creation Tests")
    class DealCreationTests {
        @Test
        @DisplayName("Should create deal successfully")
        void createDeal_Success() {
            // Given
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);
            // Validation rules return empty list (no violations)
            when(dealValidationRuleExecutorService.executeValidationRules(any(Deal.class))).thenReturn(Collections.emptyList());
            when(salesforceService.validateOpportunityExists(any())).thenReturn(true);
            when(dealRepository.existsBySalesforceOpportunityId(any())).thenReturn(false);

            // When
            Deal createdDeal = dealService.createDeal(testDeal);

            // Then
            assertThat(createdDeal).isNotNull();
            assertThat(createdDeal.getId()).isEqualTo("test-deal-1");
            verify(dealRepository).save(any(Deal.class));
        }
    }

    @Nested
    @DisplayName("Deal Retrieval Tests")
    class DealRetrievalTests {
        @Test
        @DisplayName("Should get deal by ID successfully")
        void getDealById_Success() {
            // Given
            when(dealRepository.findById("test-deal-1")).thenReturn(Optional.of(testDeal));

            // When
            Deal foundDeal = dealService.getDealById("test-deal-1");

            // Then
            assertThat(foundDeal).isNotNull();
            assertThat(foundDeal.getId()).isEqualTo("test-deal-1");
        }

        @Test
        @DisplayName("Should throw exception when deal not found")
        void getDealById_NotFound() {
            // Given
            when(dealRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> dealService.getDealById("non-existent"))
                .isInstanceOf(DealNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Deal Status Update Tests")
    class DealStatusUpdateTests {
        @Test
        @DisplayName("Should submit deal for approval successfully")
        void submitForApproval_Success() {
            // Given
            when(dealRepository.findById("test-deal-1")).thenReturn(Optional.of(testDeal));
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

            // When
            Deal submittedDeal = dealService.submitForApproval("test-deal-1");

            // Then
            assertThat(submittedDeal.getStatus()).isEqualTo(DealStatus.SUBMITTED);
            verify(dealRepository).save(any(Deal.class));
        }
    }

    @Nested
    @DisplayName("Deal Salesforce Integration Tests")
    class DealSalesforceTests {
        @Test
        @DisplayName("Should sync deal with Salesforce successfully")
        void syncWithSalesforce_Success() {
            // Given
            when(dealRepository.findById("test-deal-1")).thenReturn(Optional.of(testDeal));
            when(salesforceService.syncDealToOpportunity(any(Deal.class))).thenReturn(testDeal);
            when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

            // When
            Deal syncedDeal = dealService.syncWithSalesforce("test-deal-1");

            // Then
            assertThat(syncedDeal).isNotNull();
            verify(salesforceService).syncDealToOpportunity(any(Deal.class));
            verify(dealRepository).save(any(Deal.class));
            verify(pricingRuleEngine).evaluateRules(any(Deal.class));
        }
    }
}
