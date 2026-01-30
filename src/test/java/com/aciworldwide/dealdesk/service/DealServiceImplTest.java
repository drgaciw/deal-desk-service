package com.aciworldwide.dealdesk.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aciworldwide.dealdesk.exception.InvalidDealStateException;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.repository.DealRepository;
import com.aciworldwide.dealdesk.rules.service.DealStatusRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.DealValidationRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.TCVRuleExecutorService;
import com.aciworldwide.dealdesk.service.impl.DealServiceImpl;
import com.aciworldwide.dealdesk.util.TestDataFactory;

@ExtendWith(MockitoExtension.class)
class DealServiceImplTest {

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

    @InjectMocks
    private DealServiceImpl dealService;

    private Deal testDeal;

    @BeforeEach
    void setUp() {
        testDeal = TestDataFactory.createDeal();
    }

    @Test
    void createDeal_ValidDeal_ReturnsSavedDeal() {
        // Given
        lenient().when(dealRepository.existsBySalesforceOpportunityId(anyString())).thenReturn(false);
        when(salesforceService.validateOpportunityExists(anyString())).thenReturn(true);
        when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

        // When
        Deal result = dealService.createDeal(testDeal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(DealStatus.DRAFT);
        verify(dealRepository).save(any(Deal.class));
        verify(salesforceService).validateOpportunityExists(anyString());
    }


    @Test
    void createDeal_DuplicateOpportunityId_ThrowsException() {
        // Given
        when(salesforceService.validateOpportunityExists(anyString())).thenReturn(true);
        when(dealRepository.existsBySalesforceOpportunityId(any())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> dealService.createDeal(testDeal))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Deal already exists for opportunity");
    }

    @Test
    void approveDeal_ValidDeal_ReturnsApprovedDeal() {
        // Given
        testDeal.setStatus(DealStatus.SUBMITTED);
        when(dealRepository.findById(any())).thenReturn(Optional.of(testDeal));
        when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);
        when(salesforceService.syncDealToOpportunity(any(Deal.class))).thenReturn(testDeal);

        // When
        Deal result = dealService.approveDeal("1", "approver-1");

        // Then
        assertThat(result.getStatus()).isEqualTo(DealStatus.APPROVED);
        assertThat(result.getApprovedBy()).isEqualTo("approver-1");
        assertThat(result.getApprovedAt()).isNotNull();
        verify(salesforceService).syncDealToOpportunity(any(Deal.class));
    }

    @Test
    void approveDeal_InvalidStatus_ThrowsException() {
        // Given
        testDeal.setStatus(DealStatus.DRAFT);
        when(dealRepository.findById(any())).thenReturn(Optional.of(testDeal));

        // When/Then
        assertThatThrownBy(() -> dealService.approveDeal("1", "approver-1"))
            .isInstanceOf(InvalidDealStateException.class);
    }

    @Test
    void getDealsByStatus_ReturnsFilteredDeals() {
        // Given
        List<Deal> deals = TestDataFactory.createDeals(3);
        when(dealRepository.findByStatus(DealStatus.DRAFT)).thenReturn(deals);

        // When
        List<Deal> result = dealService.getDealsByStatus(DealStatus.DRAFT);

        // Then
        assertThat(result).hasSize(3);
        verify(dealRepository).findByStatus(DealStatus.DRAFT);
    }

    @Test
    void getHighValueDeals_ReturnsFilteredDeals() {
        // Given
        List<Deal> deals = List.of(TestDataFactory.createHighValueDeal());
        BigDecimal minValue = new BigDecimal("1000000.00");
        when(dealRepository.findHighValueDeals(minValue, DealStatus.APPROVED)).thenReturn(deals);

        // When
        List<Deal> result = dealService.getHighValueDeals(minValue, DealStatus.APPROVED);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValue()).isGreaterThanOrEqualTo(minValue);
        verify(dealRepository).findHighValueDeals(minValue, DealStatus.APPROVED);
    }

    @Test
    void findExpiredDeals_ReturnsExpiredDeals() {
        // Given
        ZonedDateTime expirationDate = ZonedDateTime.now(ZoneId.systemDefault()).minusDays(7);
        Deal expiredDeal = TestDataFactory.createDealWithStatus(DealStatus.SUBMITTED);
        expiredDeal.setUpdatedAt(ZonedDateTime.now(ZoneId.systemDefault()).minusDays(10));
        when(dealRepository.findByStatusAndUpdatedAtBefore(eq(DealStatus.SUBMITTED), eq(expirationDate))).thenReturn(List.of(expiredDeal));

        // When
        List<Deal> result = dealService.findExpiredDeals(expirationDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUpdatedAt()).isBefore(expirationDate);
    }

    @Test
    void calculateTotalValue_ReturnsSumOfDealValues() {
        // Given
        BigDecimal expectedTotal = new BigDecimal("300000.00");
        when(dealRepository.calculateTotalValueByStatus(DealStatus.APPROVED))
            .thenReturn(new com.aciworldwide.dealdesk.repository.TotalValueResult(null, expectedTotal));

        // When
        BigDecimal result = dealService.calculateTotalValue(DealStatus.APPROVED);

        // Then
        assertThat(result).isEqualTo(expectedTotal);
        verify(dealRepository).calculateTotalValueByStatus(DealStatus.APPROVED);
    }
}