package com.aciworldwide.dealdesk.rules.impl;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.tcv.PricingModel;
import com.aciworldwide.dealdesk.model.tcv.RepricingTriggers;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@Rule(name = "Credit Percentage Rules", description = "Rules for calculating credit percentages")
public class CreditPercentageRules {

    @Condition
    public boolean shouldApply(@Fact("deal") Deal deal) {
        PricingModel pricingModel = deal.getPricingModel();
        if (pricingModel == null) {
            return false;
        }
        
        List<RepricingTriggers> triggers = pricingModel.getRepricingTriggers();
        return triggers != null && !triggers.isEmpty();
    }

    @Action
    public void applyCreditPercentage(@Fact("deal") Deal deal) {
        PricingModel pricingModel = deal.getPricingModel();
        List<RepricingTriggers> triggers = pricingModel.getRepricingTriggers();
        
        BigDecimal creditPercentage = calculateCreditPercentage(triggers);
        pricingModel.setCreditPercentage(creditPercentage);
        
        log.debug("Applied credit percentage {} to deal {}", creditPercentage, deal.getId());
    }

    private BigDecimal calculateCreditPercentage(List<RepricingTriggers> triggers) {
        // Start with base credit percentage
        BigDecimal creditPercentage = BigDecimal.ZERO;
        
        for (RepricingTriggers trigger : triggers) {
            // Apply commercial credit rules
            if (trigger.isCommercialCredit()) {
                creditPercentage = creditPercentage.add(new BigDecimal("0.0275")); // 2.75% for commercial credit
            }
            
            // Apply all-credit card rules
            if (trigger.isAllCredit()) {
                creditPercentage = creditPercentage.add(new BigDecimal("0.02")); // Additional 2% for all credit cards
            }
            
            // Apply any thresholds or adjustments
            if (trigger.getCreditPercentageThreshold() != null) {
                if (trigger.isCreditPercentageAbove()) {
                    // If current percentage is above threshold, add adjustment
                    if (creditPercentage.compareTo(trigger.getCreditPercentageThreshold()) > 0) {
                        creditPercentage = creditPercentage.add(trigger.getCreditPercentageAdjustment());
                    }
                } else {
                    // If current percentage is below threshold, add adjustment
                    if (creditPercentage.compareTo(trigger.getCreditPercentageThreshold()) < 0) {
                        creditPercentage = creditPercentage.add(trigger.getCreditPercentageAdjustment());
                    }
                }
            }
        }
        
        return creditPercentage;
    }
}