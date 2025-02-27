package com.aciworldwide.dealdesk.rules.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.tcv.ContingentRevenue;
import com.aciworldwide.dealdesk.model.tcv.DiscountsAndAdjustments;
import com.aciworldwide.dealdesk.model.tcv.TCVCalculation;
import com.aciworldwide.dealdesk.rules.TCVRule;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TCVCalculationRules implements TCVRule {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    public TCVCalculation calculateTCV(Deal deal) {
        log.debug("Calculating TCV for deal: {}", deal.getId());
        
        TCVCalculation tcv = TCVCalculation.builder()
            .baseValue(calculateBaseValue(deal))
            .build();

        // Calculate discounts
        if (deal.getAdjustments() != null) {
            calculateDiscounts(tcv, deal.getAdjustments());
        }

        // Calculate contingent revenue
        if (deal.getContingentRevenue() != null) {
            calculateContingentRevenue(tcv, deal.getContingentRevenue());
        }

        // Calculate final value
        calculateFinalValue(tcv);

        deal.setTcvCalculation(tcv);
        deal.setValue(tcv.getFinalValue());
        
        return tcv;
    }

    private BigDecimal calculateBaseValue(Deal deal) {
        return deal.getComponents().stream()
            .map(component -> component.getQuantity().multiply(component.getUnitPrice()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void calculateDiscounts(TCVCalculation tcv, DiscountsAndAdjustments adjustments) {
        // Volume discount
        if (adjustments.getVolumeDiscountAmount() != null) {
            tcv.setVolumeDiscountAmount(adjustments.getVolumeDiscountAmount());
        } else if (adjustments.getVolumeDiscountPercentage() != null) {
            BigDecimal volumeDiscount = tcv.getBaseValue()
                .multiply(adjustments.getVolumeDiscountPercentage())
                .divide(HUNDRED, SCALE, ROUNDING_MODE);
            tcv.setVolumeDiscountAmount(volumeDiscount);
        }

        // Early payment discount
        if (adjustments.getEarlyPaymentDiscountAmount() != null) {
            tcv.setEarlyPaymentDiscountAmount(adjustments.getEarlyPaymentDiscountAmount());
        } else if (adjustments.getEarlyPaymentDiscountPercentage() != null) {
            BigDecimal earlyPaymentDiscount = tcv.getBaseValue()
                .multiply(adjustments.getEarlyPaymentDiscountPercentage())
                .divide(HUNDRED, SCALE, ROUNDING_MODE);
            tcv.setEarlyPaymentDiscountAmount(earlyPaymentDiscount);
        }

        // Total adjustments
        BigDecimal totalAdjustments = BigDecimal.ZERO;
        if (adjustments.getPriceAdjustmentAmount() != null) {
            totalAdjustments = totalAdjustments.add(adjustments.getPriceAdjustmentAmount());
        }
        if (adjustments.getOtherDiscountAmount() != null) {
            totalAdjustments = totalAdjustments.add(adjustments.getOtherDiscountAmount());
        }
        tcv.setTotalAdjustments(totalAdjustments);
    }

    private void calculateContingentRevenue(TCVCalculation tcv, ContingentRevenue contingentRevenue) {
        if (contingentRevenue.isIncludedInTCV() && contingentRevenue.getAmount() != null) {
            tcv.setContingentRevenueAmount(contingentRevenue.getAmount());
        } else {
            tcv.setContingentRevenueAmount(BigDecimal.ZERO);
        }
    }

    private void calculateFinalValue(TCVCalculation tcv) {
        BigDecimal finalValue = tcv.getBaseValue();
        
        // Subtract discounts
        if (tcv.getVolumeDiscountAmount() != null) {
            finalValue = finalValue.subtract(tcv.getVolumeDiscountAmount());
        }
        if (tcv.getEarlyPaymentDiscountAmount() != null) {
            finalValue = finalValue.subtract(tcv.getEarlyPaymentDiscountAmount());
        }
        
        // Add/subtract adjustments
        if (tcv.getTotalAdjustments() != null) {
            finalValue = finalValue.add(tcv.getTotalAdjustments());
        }
        
        // Add contingent revenue
        if (tcv.getContingentRevenueAmount() != null) {
            finalValue = finalValue.add(tcv.getContingentRevenueAmount());
        }
        
        tcv.setFinalValue(finalValue);
    }
}