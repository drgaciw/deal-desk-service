package com.aciworldwide.dealdesk.mapper;

import com.aciworldwide.dealdesk.dto.DealRequestDTO;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class DealMapperHelper {

    private final CurrencyService currencyService;

    @Autowired
    public DealMapperHelper(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public BigDecimal convertCurrency(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if (currencyCode == null || currencyCode.isEmpty()) {
            return amount; // Default to USD
        }
        
        return amount.multiply(currencyService.getConversionRate(currencyCode));
    }

    public BigDecimal resolveValue(DealRequestDTO dto) {
        return convertCurrency(dto.getValue(), dto.getCurrency());
    }

    public int calculateDaysInStatus(Deal deal) {
        if (deal.getStatusChangedAt() == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(
                deal.getStatusChangedAt(), 
                ZonedDateTime.now()
        );
    }

    public String determineNextAction(DealStatus status) {
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
