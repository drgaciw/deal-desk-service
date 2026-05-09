package com.aciworldwide.dealdesk.rules.fact;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.tcv.PricingModel;
import org.jeasy.rules.api.Facts;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DealFactProvider implements FactProvider {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

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

        PricingModel pricingModel = deal.getPricingModel();
        if (pricingModel != null) {
            facts.put("commercialCreditPercentage", getPercentageFact(pricingModel.getCommercialCreditPercentage()));
            facts.put("allCreditPercentage", getPercentageFact(pricingModel.getAllCreditPercentage()));
            facts.put("debitPercentage", getDebitPercentage(pricingModel));
            facts.put("durbinRegPercentage", getPercentageFact(pricingModel.getDurbinRegulatedPercentage()));
            facts.put("averagePayment", getOrZero(pricingModel.getAveragePayment()));
        }

        facts.put("rulesApplied", false);
    }

    private BigDecimal getDebitPercentage(PricingModel pricingModel) {
        if (pricingModel.getDebitPercentage() != null) {
            return getPercentageFact(pricingModel.getDebitPercentage());
        }

        BigDecimal creditPercentage = pricingModel.getCreditPercentage();
        if (creditPercentage != null) {
            return ONE_HUNDRED.subtract(getPercentageFact(creditPercentage));
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal getPercentageFact(BigDecimal value) {
        BigDecimal percentage = getOrZero(value);
        if (percentage.compareTo(BigDecimal.ZERO) >= 0 && percentage.compareTo(BigDecimal.ONE) <= 0) {
            return percentage.multiply(ONE_HUNDRED);
        }
        return percentage;
    }

    private BigDecimal getOrZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
