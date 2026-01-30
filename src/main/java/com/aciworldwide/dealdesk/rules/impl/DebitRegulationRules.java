package com.aciworldwide.dealdesk.rules.impl;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.tcv.RepricingTriggers;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Component
public class DebitRegulationRules {

    @Rule(name = "debit-percentage", description = "Debit Percentage Rule")
    @Component
    public static class DebitPercentageRule {
        
        @Condition
        public boolean evaluate(
                @Fact("deal") Deal deal,
                @Fact("debitPercentage") BigDecimal percentage) {
                
            List<RepricingTriggers> triggersList = deal.getPricingModel().getRepricingTriggers();
            if (triggersList == null || triggersList.isEmpty() || triggersList.get(0).getDebitPercentageThreshold() == null) {
                return false;
            }

            RepricingTriggers trigger = triggersList.get(0);
            BigDecimal threshold = trigger.getDebitPercentageThreshold();
            boolean isAboveThreshold = percentage.compareTo(threshold) > 0;
            
            // For debit percentage, we typically want to trigger when below threshold
            return trigger.isDebitPercentageAbove() == isAboveThreshold;
        }

        @Action
        public void execute(
                @Fact("deal") Deal deal,
                @Fact("debitPercentage") BigDecimal percentage,
                @Fact("averagePayment") BigDecimal averagePayment,
                @Fact("rulesApplied") Boolean rulesApplied) {
                
            log.info("Executing debit percentage rule for deal: {}", deal.getId());
            adjustPricing(deal, percentage, averagePayment, new BigDecimal("0.025")); // 2.5% adjustment
            rulesApplied = true;
        }
    }

    @Rule(name = "durbin-regulated", description = "Durbin Regulated Percentage Rule")
    public static class DurbinRegulatedRule {
        
        @Condition
        public boolean evaluate(
                @Fact("deal") Deal deal,
                @Fact("durbinRegPercentage") BigDecimal percentage) {
                
            List<RepricingTriggers> triggersList = deal.getPricingModel().getRepricingTriggers();
            if (triggersList == null || triggersList.isEmpty() || triggersList.get(0).getDurbinRegPercentageThreshold() == null) {
                return false;
            }

            RepricingTriggers trigger = triggersList.get(0);
            BigDecimal threshold = trigger.getDurbinRegPercentageThreshold();
            boolean isAboveThreshold = percentage.compareTo(threshold) > 0;
            
            // For Durbin regulated transactions, we typically want to trigger when below threshold
            return trigger.isDurbinRegPercentageAbove() == isAboveThreshold;
        }

        @Action
        public void execute(
                @Fact("deal") Deal deal,
                @Fact("durbinRegPercentage") BigDecimal percentage,
                @Fact("averagePayment") BigDecimal averagePayment,
                @Fact("rulesApplied") Boolean rulesApplied) {
                
            log.info("Executing Durbin regulated percentage rule for deal: {}", deal.getId());
            adjustPricing(deal, percentage, averagePayment, new BigDecimal("0.03")); // 3% adjustment
            rulesApplied = true;
        }
    }

    private static void adjustPricing(Deal deal, BigDecimal percentage, BigDecimal averagePayment, BigDecimal adjustmentRate) {
        BigDecimal currentValue = deal.getValue();
        if (currentValue != null) {
            // Refined logic:
            // 1. Calculate adjustment proportional to the volume percentage
            BigDecimal volumePercentage = percentage != null ? percentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal proportionalAdjustment = currentValue.multiply(volumePercentage).multiply(adjustmentRate);
            
            // 2. Calculate fixed fee component
            BigDecimal fixedFeeTotal = BigDecimal.ZERO;
            
            if (averagePayment != null && averagePayment.compareTo(BigDecimal.ZERO) > 0) {
                // Estimate affected transaction count
                // Transactions = (Total Value / Average Payment) * Volume Percentage
                BigDecimal totalTransactions = currentValue.divide(averagePayment, 0, RoundingMode.HALF_UP);
                BigDecimal affectedTransactions = totalTransactions.multiply(volumePercentage);

                BigDecimal fixedFeePerTrans = new BigDecimal("0.21"); // $0.21 fixed fee per Durbin regulation
                fixedFeeTotal = affectedTransactions.multiply(fixedFeePerTrans);
            } else {
                // Fallback to simple fixed fee if average payment is unknown
                fixedFeeTotal = new BigDecimal("0.21");
            }

            deal.setValue(currentValue.add(proportionalAdjustment).add(fixedFeeTotal));
            
            log.info("Applied refined debit/Durbin adjustment to deal: {}, new value: {} (adj: {}, fixed: {})",
                    deal.getId(), deal.getValue(), proportionalAdjustment, fixedFeeTotal);
        }
    }
}
