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
                @Fact("rulesApplied") Boolean rulesApplied) {
                
            log.info("Executing debit percentage rule for deal: {}", deal.getId());
            adjustPricing(deal, new BigDecimal("0.025")); // 2.5% adjustment
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
                @Fact("rulesApplied") Boolean rulesApplied) {
                
            log.info("Executing Durbin regulated percentage rule for deal: {}", deal.getId());
            adjustPricing(deal, new BigDecimal("0.03")); // 3% adjustment
            rulesApplied = true;
        }
    }

    private static void adjustPricing(Deal deal, BigDecimal adjustmentRate) {
        BigDecimal currentValue = deal.getValue();
        if (currentValue != null) {
            // For debit and Durbin rules, we might want to apply different pricing logic
            // Here we're applying a straight percentage adjustment, but this could be more complex
            BigDecimal adjustment = currentValue.multiply(adjustmentRate);
            
            // For debit cards, we might want to add a fixed fee component
            BigDecimal fixedFee = new BigDecimal("0.21"); // $0.21 fixed fee per Durbin regulation
            
            deal.setValue(currentValue.add(adjustment).add(fixedFee));
            
            log.info("Applied debit/Durbin regulation adjustment to deal: {}, new value: {}", 
                    deal.getId(), deal.getValue());
        }
    }
}