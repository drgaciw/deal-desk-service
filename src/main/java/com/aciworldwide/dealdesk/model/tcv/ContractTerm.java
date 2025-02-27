package com.aciworldwide.dealdesk.model.tcv;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class ContractTerm {
    private LocalDate startDate;
    private LocalDate endDate;
    private int durationMonths;
    private BigDecimal monthlyValue;
    private BigDecimal totalValue;
    private boolean autoRenew;
    private int renewalTermMonths;
    private BigDecimal renewalValue;
    private String terminationNoticeMonths;
    private String paymentTerms;
    private String billingFrequency;
    private String billingTerms;
    
    @Builder.Default
    private boolean earlyTerminationAllowed = false;
    private BigDecimal earlyTerminationFee;
    
    public void calculateValues() {
        if (startDate != null && endDate != null) {
            durationMonths = (int) (endDate.toEpochDay() - startDate.toEpochDay()) / 30;
        }
        
        if (monthlyValue != null) {
            totalValue = monthlyValue.multiply(BigDecimal.valueOf(durationMonths));
        }
    }
    
    public void validate() {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        if (monthlyValue == null || monthlyValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monthly value must be greater than zero");
        }
        
        if (earlyTerminationAllowed && (earlyTerminationFee == null || earlyTerminationFee.compareTo(BigDecimal.ZERO) < 0)) {
            throw new IllegalArgumentException("Early termination fee must be non-negative when early termination is allowed");
        }
    }
}