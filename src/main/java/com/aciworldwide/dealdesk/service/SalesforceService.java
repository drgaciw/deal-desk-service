package com.aciworldwide.dealdesk.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

import com.aciworldwide.dealdesk.exception.SalesforceIntegrationException;
import com.aciworldwide.dealdesk.model.Deal;

/**
 * Service interface for Salesforce integration operations.
 * Provides methods for authentication, opportunity management, deal synchronization,
 * CPQ operations, and other Salesforce-related functionality.
 */
public interface SalesforceService {

    // Authentication Operations

    /**
     * Authenticates with Salesforce and returns a session ID.
     *
     * @return The session ID for authenticated access
     * @throws SalesforceIntegrationException if authentication fails
     */
    @Retryable(
            value = SalesforceIntegrationException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    String authenticate();

    /**
     * Validates if the given session ID is still valid.
     *
     * @param sessionId The session ID to validate
     * @return true if session is valid, false otherwise
     */
    boolean validateSession(String sessionId);

    /**
     * Refreshes the OAuth token for continued access.
     *
     * @throws SalesforceIntegrationException if token refresh fails
     */
    @Retryable(
            value = SalesforceIntegrationException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    void refreshToken();

    // Opportunity Operations

    /**
     * Retrieves opportunity details from Salesforce.
     *
     * @param opportunityId The Salesforce opportunity ID
     * @return Map of opportunity fields and values
     * @throws SalesforceIntegrationException if retrieval fails
     */
    Map<String, Object> getOpportunity(String opportunityId);

    void updateOpportunity(String opportunityId, Map<String, Object> fields);

    boolean validateOpportunityExists(String opportunityId);

    List<String> getOpportunityProducts(String opportunityId);

    // Deal Sync Operations
    @Retryable(
            value = SalesforceIntegrationException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    Deal syncDealToOpportunity(Deal deal);

    @Recover
    Deal handleSyncFailure(SalesforceIntegrationException e, Deal deal);

    Deal updateDealFromOpportunity(Deal deal);

    void syncQuotePricing(Deal deal);

    // CPQ Operations
    String createQuote(String opportunityId, Deal deal);

    void updateQuote(String quoteId, Deal deal);

    Map<String, Object> getQuote(String quoteId);

    List<Map<String, Object>> getQuoteLineItems(String quoteId);

    void updateQuoteLineItems(String quoteId, List<Map<String, Object>> lineItems);

    BigDecimal calculateQuoteTotalPrice(String quoteId);

    @Retryable(
            value = SalesforceIntegrationException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    void validateQuoteExists(String quoteId);

    @Retryable(
            value = SalesforceIntegrationException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    void syncPriceRules(String priceRuleId, String quoteId);

    // Approval Operations
    void approveQuote(String quoteId, String approverId);

    void rejectQuote(String quoteId, String rejectorId, String reason);

    List<Map<String, Object>> getApprovalHistory(String quoteId);

    boolean isApprovalRequired(Deal deal);

    void submitForApproval(String quoteId, Deal deal);

    List<String> getApprovers(String quoteId);

    // Product Operations
    List<Map<String, Object>> getProducts(List<String> productCodes);

    Map<String, BigDecimal> getProductPrices(List<String> productCodes, String pricebookId);

    void validateProducts(List<String> productCodes);

    // Batch Operations
    void batchUpdateOpportunities(List<Deal> deals);

    void batchUpdateQuotes(List<Deal> deals);

    // Metadata Operations
    Map<String, String> getFieldMappings(String objectName);

    List<Map<String, Object>> describeObject(String objectName);

    // Error Handling
    String getLastError();

    void clearErrors();

    // Cache Operations
    void invalidateCache(String objectType, String recordId);

    void warmupCache(String objectType, List<String> recordIds);

    // Monitoring
    Map<String, Object> getApiLimits();

    Map<String, Object> getSystemStatus();

    // Utility Methods
    boolean isValidSalesforceId(String id);

    String formatSalesforceId(String id);

    Map<String, Object> convertDealToSalesforceFields(Deal deal);

    void updateDealFromSalesforceFields(Deal deal, Map<String, Object> fields);

    // CPQ Price Rules
    List<Map<String, Object>> evaluatePriceRules(String quoteId);

    void applyPriceRules(String quoteId);

    void validatePriceRules(String quoteId);

    // CPQ Documents
    byte[] generateQuoteDocument(String quoteId, String templateId);

    List<Map<String, Object>> getAvailableTemplates(String quoteId);

    void attachDocument(String quoteId, String name, byte[] content, String contentType);

    @Recover
    default String handleAuthenticationFailure(SalesforceIntegrationException e) {
        // Handle auth failure gracefully
        return "";
    }
}

