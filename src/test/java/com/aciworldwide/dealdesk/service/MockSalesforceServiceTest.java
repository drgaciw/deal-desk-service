package com.aciworldwide.dealdesk.service;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.util.TestDataFactory;

class MockSalesforceServiceTest {

    private MockSalesforceService salesforceService;
    private Deal testDeal;

    @BeforeEach
    void setUp() {
        salesforceService = new MockSalesforceService();
        testDeal = TestDataFactory.createDeal();
    }

    @Test
    void authenticate_ReturnsValidSessionId() {
        // When
        String sessionId = salesforceService.authenticate();

        // Then
        assertThat(sessionId).isNotNull();
        assertThat(salesforceService.validateSession(sessionId)).isTrue();
    }

    @Test
    void validateOpportunityExists_ValidOpportunityId_ReturnsTrue() {
        // Given
        String opportunityId = "OPP-123";

        // When
        boolean exists = salesforceService.validateOpportunityExists(opportunityId);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void syncDealToOpportunity_ValidDeal_UpdatesOpportunity() {
        // When
        Deal syncedDeal = salesforceService.syncDealToOpportunity(testDeal);

        // Then
        Map<String, Object> opportunity = salesforceService.getOpportunity(testDeal.getSalesforceOpportunityId());
        assertThat(syncedDeal).isNotNull();
        assertThat(opportunity).isNotEmpty();
        assertThat(opportunity.get("Name")).isEqualTo(testDeal.getName());
        assertThat(opportunity.get("Amount")).isEqualTo(testDeal.getValue());
    }

    @Test
    void createQuote_ValidDeal_ReturnsQuoteId() {
        // When
        String quoteId = salesforceService.createQuote(testDeal.getSalesforceOpportunityId(), testDeal);

        // Then
        assertThat(quoteId).isNotNull();
        assertThat(quoteId).startsWith("QT-");
        assertThat(salesforceService.getQuote(quoteId)).isNotEmpty();
    }

    @Test
    void isApprovalRequired_HighValueDeal_ReturnsTrue() {
        // Given
        testDeal.setValue(new BigDecimal("150000.00"));

        // When
        boolean requiresApproval = salesforceService.isApprovalRequired(testDeal);

        // Then
        assertThat(requiresApproval).isTrue();
    }

    @Test
    void isApprovalRequired_LowValueDeal_ReturnsFalse() {
        // Given
        testDeal.setValue(new BigDecimal("50000.00"));

        // When
        boolean requiresApproval = salesforceService.isApprovalRequired(testDeal);

        // Then
        assertThat(requiresApproval).isFalse();
    }

    @Test
    void getApprovers_ReturnsDefaultApprovers() {
        // When
        List<String> approvers = salesforceService.getApprovers("any-quote-id");

        // Then
        assertThat(approvers)
            .isNotEmpty()
            .contains("approver1", "approver2");
    }

    @Test
    void getApiLimits_ReturnsDefaultLimits() {
        // When
        Map<String, Object> limits = salesforceService.getApiLimits();

        // Then
        assertThat(limits).isNotEmpty();
        @SuppressWarnings("unchecked")
        Map<String, Integer> dailyApiRequests = (Map<String, Integer>) limits.get("DailyApiRequests");
        assertThat(dailyApiRequests).containsKey("Max").containsKey("Remaining");
    }

    @Test
    void batchUpdateOpportunities_UpdatesAllDeals() {
        // Given
        List<Deal> deals = TestDataFactory.createDeals(3);

        // When
        salesforceService.batchUpdateOpportunities(deals);

        // Then
        deals.forEach(deal -> {
            Map<String, Object> opportunity = salesforceService.getOpportunity(deal.getSalesforceOpportunityId());
            assertThat(opportunity).isNotEmpty();
            assertThat(opportunity.get("Name")).isEqualTo(deal.getName());
        });
    }

    @Test
    void convertDealToSalesforceFields_ReturnsExpectedMapping() {
        // When
        Map<String, Object> fields = salesforceService.convertDealToSalesforceFields(testDeal);

        // Then
        assertThat(fields)
            .containsEntry("Name", testDeal.getName())
            .containsEntry("Amount", testDeal.getValue())
            .containsEntry("StageName", testDeal.getStatus().name())
            .containsEntry("AccountId", testDeal.getAccountId());
    }
}