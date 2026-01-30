package com.aciworldwide.dealdesk.rules.fact;

import com.aciworldwide.dealdesk.model.Deal;
import org.jeasy.rules.api.Facts;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class DealFactProvider implements FactProvider {
    
    @Override
    public Class<?> getSupportedContextType() {
        return Deal.class;
    }

    @Override
    public void provideFacts(Object context, Facts facts) {
        if (!(context instanceof Deal deal)) {
            throw new IllegalArgumentException("Context must be a Deal instance");
        }

        facts.put("deal", deal);
        
        // Add transaction percentages for rule evaluation
        if (deal.getPricingModel() != null) {
            facts.put("commercialCreditPercentage", calculateCommercialCreditPercentage(deal));
            facts.put("allCreditPercentage", calculateAllCreditPercentage(deal));
            facts.put("debitPercentage", calculateDebitPercentage(deal));
            facts.put("durbinRegPercentage", calculateDurbinRegulatedPercentage(deal));
            facts.put("averagePayment", calculateAveragePayment(deal));
        }
        
        // Initialize rules applied flag
        facts.put("rulesApplied", false);
    }

    private BigDecimal calculateCommercialCreditPercentage(Deal deal) {
        if (deal.getPricingModel() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal val = deal.getPricingModel().getCommercialCreditPercentage();
        return val != null ? val : BigDecimal.ZERO;
    }

    private BigDecimal calculateAllCreditPercentage(Deal deal) {
        if (deal.getPricingModel() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal val = deal.getPricingModel().getAllCreditPercentage();
        return val != null ? val : BigDecimal.ZERO;
    }

    private BigDecimal calculateDebitPercentage(Deal deal) {
        if (deal.getPricingModel() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal val = deal.getPricingModel().getDebitPercentage();
        return val != null ? val : BigDecimal.ZERO;
    }

    private BigDecimal calculateDurbinRegulatedPercentage(Deal deal) {
        if (deal.getPricingModel() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal val = deal.getPricingModel().getDurbinRegulatedPercentage();
        return val != null ? val : BigDecimal.ZERO;
    }

    private BigDecimal calculateAveragePayment(Deal deal) {
        if (deal.getPricingModel() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal val = deal.getPricingModel().getAveragePayment();
        return val != null ? val : BigDecimal.ZERO;
    }
}
