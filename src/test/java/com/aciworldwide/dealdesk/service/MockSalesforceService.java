package com.aciworldwide.dealdesk.service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.aciworldwide.dealdesk.exception.SalesforceIntegrationException;
import com.aciworldwide.dealdesk.model.Deal;

import lombok.extern.slf4j.Slf4j;

@Service
@Primary
@Profile("test")
@Slf4j
public class MockSalesforceService implements SalesforceService {
    
    private final Map<String, Map<String, Object>> opportunities = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> quotes = new ConcurrentHashMap<>();
    private String sessionId;
    private String lastError;
    private boolean shouldFail = false;

    public void setShouldFail(boolean shouldFail) {
        this.shouldFail = shouldFail;
    }

    @Override
    public String authenticate() {
        if (shouldFail) {
            throw new SalesforceIntegrationException("Mock authentication failure");
        }
        sessionId = UUID.randomUUID().toString();
        return sessionId;
    }

    @Override
    public boolean validateSession(String sessionId) {
        return !shouldFail && this.sessionId != null && this.sessionId.equals(sessionId);
    }

    @Override
    public void refreshToken() {
        if (shouldFail) {
            throw new SalesforceIntegrationException("Mock token refresh failure");
        }
        authenticate();
    }

    @Override
    public Map<String, Object> getOpportunity(String opportunityId) {
        return opportunities.getOrDefault(opportunityId, new HashMap<>());
    }

    @Override
    public void updateOpportunity(String opportunityId, Map<String, Object> fields) {
        opportunities.put(opportunityId, new HashMap<>(fields));
    }

    @Override
    public boolean validateOpportunityExists(String opportunityId) {
        // For tests, simply return true (or implement logic as needed)
        return opportunityId != null && !opportunityId.isEmpty();
    }

    @Override
    public List<String> getOpportunityProducts(String opportunityId) {
        return new ArrayList<>();
    }

    @Override
    public Deal syncDealToOpportunity(Deal deal) {
        if (shouldFail) {
            throw new SalesforceIntegrationException("Mock sync failure");
        }
        log.debug("Mock syncing deal {} to Salesforce", deal.getId());
        Map<String, Object> fields = convertDealToSalesforceFields(deal);
        updateOpportunity(deal.getSalesforceOpportunityId(), fields);
        return deal;
    }

    @Override
    public Deal updateDealFromOpportunity(Deal deal) {
        Map<String, Object> fields = getOpportunity(deal.getSalesforceOpportunityId());
        updateDealFromSalesforceFields(deal, fields);
        return deal;
    }

    @Override
    public void syncQuotePricing(Deal deal) {
        if (shouldFail) {
            throw new SalesforceIntegrationException("Mock quote pricing sync failure");
        }
    }

    @Override
    public String createQuote(String opportunityId, Deal deal) {
        String quoteId = "QT-" + UUID.randomUUID();
        quotes.put(quoteId, convertDealToSalesforceFields(deal));
        return quoteId;
    }

    @Override
    public void updateQuote(String quoteId, Deal deal) {
        quotes.put(quoteId, convertDealToSalesforceFields(deal));
    }

    @Override
    public Map<String, Object> getQuote(String quoteId) {
        return quotes.getOrDefault(quoteId, new HashMap<>());
    }

    @Override
    public List<Map<String, Object>> getQuoteLineItems(String quoteId) {
        return new ArrayList<>();
    }

    @Override
    public void updateQuoteLineItems(String quoteId, List<Map<String, Object>> lineItems) {
        // Mock implementation
    }

    @Override
    public BigDecimal calculateQuoteTotalPrice(String quoteId) {
        return BigDecimal.valueOf(100000);
    }

    @Override
    public void approveQuote(String quoteId, String approverId) {
        // Mock implementation
    }

    @Override
    public void rejectQuote(String quoteId, String rejectorId, String reason) {
        // Mock implementation
    }

    @Override
    public List<Map<String, Object>> getProducts(List<String> productCodes) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, BigDecimal> getProductPrices(List<String> productCodes, String pricebookId) {
        return new HashMap<>();
    }

    @Override
    public void validateProducts(List<String> productCodes) {
        // Mock implementation
    }

    @Override
    public void batchUpdateOpportunities(List<Deal> deals) {
        deals.forEach(this::syncDealToOpportunity);
    }

    @Override
    public void batchUpdateQuotes(List<Deal> deals) {
        // Mock implementation
    }

    @Override
    public Map<String, String> getFieldMappings(String objectName) {
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> describeObject(String objectName) {
        return new ArrayList<>();
    }

    @Override
    public String getLastError() {
        return lastError;
    }

    @Override
    public void clearErrors() {
        lastError = null;
    }

    @Override
    public void invalidateCache(String objectType, String recordId) {
        // Mock implementation
    }

    @Override
    public void warmupCache(String objectType, List<String> recordIds) {
        // Mock implementation
    }

    @Override
    public Map<String, Object> getApiLimits() {
        return Map.of(
            "DailyApiRequests", Map.of(
                "Max", 15000,
                "Remaining", 14000
            )
        );
    }

    @Override
    public Map<String, Object> getSystemStatus() {
        return Map.of("status", "OK");
    }

    @Override
    public boolean isValidSalesforceId(String id) {
        return id != null && (id.startsWith("OPP-") || id.startsWith("QT-"));
    }

    @Override
    public String formatSalesforceId(String id) {
        return id;
    }

    @Override
    public Map<String, Object> convertDealToSalesforceFields(Deal deal) {
        return Map.of(
            "Name", deal.getName(),
            "Amount", deal.getValue(),
            "StageName", deal.getStatus().name(),
            "AccountId", deal.getAccountId()
        );
    }

    @Override
    public void updateDealFromSalesforceFields(Deal deal, Map<String, Object> fields) {
        // Mock implementation
    }

    @Override
    public List<Map<String, Object>> evaluatePriceRules(String quoteId) {
        return new ArrayList<>();
    }

    @Override
    public void applyPriceRules(String quoteId) {
        // Mock implementation
    }

    @Override
    public void validatePriceRules(String quoteId) {
        // Mock implementation
    }

    @Override
    public List<Map<String, Object>> getApprovalHistory(String quoteId) {
        return new ArrayList<>();
    }

    @Override
    public boolean isApprovalRequired(Deal deal) {
        return deal.getValue().compareTo(new BigDecimal("100000")) >= 0;
    }

    @Override
    public void submitForApproval(String quoteId, Deal deal) {
        // Mock implementation
    }

    @Override
    public List<String> getApprovers(String quoteId) {
        return List.of("approver1", "approver2");
    }

    @Override
    public byte[] generateQuoteDocument(String quoteId, String templateId) {
        return new byte[0];
    }

    @Override
    public List<Map<String, Object>> getAvailableTemplates(String quoteId) {
        return new ArrayList<>();
    }

    @Override
    public void attachDocument(String quoteId, String name, byte[] content, String contentType) {
        // Mock implementation
    }

    @Override
    public Deal handleSyncFailure(SalesforceIntegrationException e, Deal deal) {
        log.error("Mock handling sync failure for deal {}: {}", deal.getId(), e.getMessage());
        deal.setSynced(false);
        deal.setSyncError(e.getMessage());
        deal.setLastSyncAt(ZonedDateTime.now(ZoneId.systemDefault()));
        return deal;
    }

    @Override
    public void handleBatchUpdateFailure(SalesforceIntegrationException e, List<Deal> deals) {
        log.error("Mock handling batch update failure for {} deals: {}", deals.size(), e.getMessage());
        // Mark all deals as failed to sync
        deals.forEach(deal -> {
            deal.setSynced(false);
            deal.setSyncError(e.getMessage());
            deal.setLastSyncAt(ZonedDateTime.now(ZoneId.systemDefault()));
        });
    }

    @Override
    public void validateQuoteExists(String quoteId) {
        if (shouldFail) {
            throw new SalesforceIntegrationException("Mock quote validation failure");
        }
        log.debug("Mock validating quote {}", quoteId);
    }

    @Override
    public void syncPriceRules(String priceRuleId, String quoteId) {
        if (shouldFail) {
            throw new SalesforceIntegrationException("Mock price rule sync failure");
        }
        log.debug("Mock syncing price rule {} to quote {}", priceRuleId, quoteId);
    }
}