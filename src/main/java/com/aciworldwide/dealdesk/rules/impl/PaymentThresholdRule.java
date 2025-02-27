package com.aciworldwide.dealdesk.rules.impl;

import com.aciworldwide.dealdesk.model.Deal;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Rule(
    name = "Payment Threshold Rule",
    description = "Applies when average payment is below $5000",
    priority = 1
)
@Component
public class PaymentThresholdRule {

    private static final BigDecimal THRESHOLD = new BigDecimal("5000.00");

    @Condition
    public boolean shouldApply(@Fact("deal") Deal deal) {
        return deal.getPricingModel() != null &&
               deal.getPricingModel().getAveragePayment() != null &&
               deal.getPricingModel().getAveragePayment().compareTo(THRESHOLD) < 0;
    }

    @Action
    public void applyThreshold(Facts facts) {
        Deal deal = facts.get("deal");
        // Apply threshold logic here
        System.out.println("Applying payment threshold rule for deal: " + deal.getId());
    }
}