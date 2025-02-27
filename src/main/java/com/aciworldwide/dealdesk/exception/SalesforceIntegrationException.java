package com.aciworldwide.dealdesk.exception;

import java.util.Map;

public class SalesforceIntegrationException extends DealDeskException {
    
    public SalesforceIntegrationException(String message) {
        super(message, "SALESFORCE_ERROR", Map.of());
    }

    public SalesforceIntegrationException(String message, String errorCode, Map<String, Object> apiResponse) {
        super(
            message,
            "SALESFORCE_" + errorCode,
            Map.of(
                "errorCode", errorCode,
                "apiResponse", apiResponse
            )
        );
    }

    public SalesforceIntegrationException(String message, String errorCode, Throwable cause) {
        super(
            message,
            "SALESFORCE_" + errorCode,
            Map.of("errorCode", errorCode),
            cause
        );
    }

    public static SalesforceIntegrationException authenticationError(String message) {
        return new SalesforceIntegrationException(
            message,
            "AUTH_ERROR",
            Map.of("timestamp", System.currentTimeMillis())
        );
    }

    public static SalesforceIntegrationException quoteSyncError(String quoteId, String error) {
        return new SalesforceIntegrationException(
            String.format("Failed to sync quote %s: %s", quoteId, error),
            "QUOTE_SYNC_ERROR",
            Map.of(
                "quoteId", quoteId,
                "error", error,
                "timestamp", System.currentTimeMillis()
            )
        );
    }

    public static SalesforceIntegrationException opportunityNotFound(String opportunityId) {
        return new SalesforceIntegrationException(
            String.format("Salesforce opportunity not found: %s", opportunityId),
            "OPPORTUNITY_NOT_FOUND",
            Map.of("opportunityId", opportunityId)
        );
    }

    public static SalesforceIntegrationException invalidResponse(String operation, Map<String, Object> response) {
        return new SalesforceIntegrationException(
            String.format("Invalid Salesforce response for operation: %s", operation),
            "INVALID_RESPONSE",
            Map.of(
                "operation", operation,
                "response", response
            )
        );
    }

    public static SalesforceIntegrationException apiLimitExceeded(String limitType, Map<String, Object> limits) {
        return new SalesforceIntegrationException(
            String.format("Salesforce API limit exceeded for: %s", limitType),
            "API_LIMIT_EXCEEDED",
            Map.of(
                "limitType", limitType,
                "limits", limits
            )
        );
    }

    public static SalesforceIntegrationException timeoutError(String operation, long durationMs) {
        return new SalesforceIntegrationException(
            String.format("Operation timed out after %d ms: %s", durationMs, operation),
            "TIMEOUT",
            Map.of(
                "operation", operation,
                "durationMs", durationMs
            )
        );
    }
}