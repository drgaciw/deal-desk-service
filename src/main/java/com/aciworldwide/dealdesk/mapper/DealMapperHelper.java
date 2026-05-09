package com.aciworldwide.dealdesk.mapper;

import com.aciworldwide.dealdesk.dto.DealRequestDTO;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class DealMapperHelper {

    private final CurrencyService currencyService;

    @Named("convertValue")
    public BigDecimal convertValue(DealRequestDTO dto) {
        return resolveValue(dto);
    }

    public BigDecimal resolveValue(DealRequestDTO dto) {
        if (dto == null) {
            return BigDecimal.ZERO;
        }
        return convertCurrency(dto.getValue(), dto.getCurrency());
    }

    public BigDecimal convertCurrency(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return currencyService.convertToUSD(amount, currencyCode);
    }

    public BigDecimal resolveValue(DealRequestDTO dto) {
        return convertValue(dto);
    }

    @Named("calculateDaysInStatus")
    public int calculateDaysInStatus(Deal deal) {
        if (deal.getStatusChangedAt() == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(
                deal.getStatusChangedAt(), 
                ZonedDateTime.now()
        );
    }

    @Named("determineNextAction")
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
