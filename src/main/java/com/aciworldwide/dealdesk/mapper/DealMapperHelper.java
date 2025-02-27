package com.aciworldwide.dealdesk.mapper;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class DealMapperHelper {

    public static BigDecimal convertCurrency(BigDecimal amount, String currencyCode) {
        if (currencyCode == null || currencyCode.isEmpty()) {
            return amount; // Default to USD
        }
        
        // Implementation would use external currency service
        // This is a placeholder for the actual conversion logic
        return amount.multiply(getConversionRate(currencyCode));
    }

    private static BigDecimal getConversionRate(String currencyCode) {
        // Fetch conversion rate from external service or cache
        return BigDecimal.ONE; // Placeholder
    }

    public static int calculateDaysInStatus(Deal deal) {
        if (deal.getStatusChangedAt() == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(
                deal.getStatusChangedAt(), 
                ZonedDateTime.now()
        );
    }

    public static String determineNextAction(DealStatus status) {
        if (status == null) {
            return "No Action";
        }
        
        switch (status) {
            case DRAFT: return "Submit for Approval";
            case SUBMITTED: return "Review Details";
            case APPROVED: return "Sync with Salesforce";
            default: return "No Action";
        }
    }
}