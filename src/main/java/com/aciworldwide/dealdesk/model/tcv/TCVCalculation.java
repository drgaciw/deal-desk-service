package com.aciworldwide.dealdesk.model.tcv;

import java.math.BigDecimal;

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
public class TCVCalculation {
    private BigDecimal baseValue;
    private BigDecimal volumeDiscountAmount;
    private BigDecimal earlyPaymentDiscountAmount;
    private BigDecimal totalAdjustments;
    private BigDecimal contingentRevenueAmount;
    private BigDecimal finalValue;
}