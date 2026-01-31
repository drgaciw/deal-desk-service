package com.aciworldwide.dealdesk.model.tcv;

import java.math.BigDecimal;
import java.util.List;

public class PricingModel {
    private BigDecimal averagePayment;
    private List<RepricingTriggers> repricingTriggers;
    private BigDecimal creditPercentage;
    private BigDecimal commercialCreditPercentage;
    private BigDecimal durbinRegulatedPercentage;

    public BigDecimal getAveragePayment() {
        return averagePayment;
    }

    public void setAveragePayment(BigDecimal averagePayment) {
        this.averagePayment = averagePayment;
    }

    public List<RepricingTriggers> getRepricingTriggers() {
        return repricingTriggers;
    }

    public void setRepricingTriggers(List<RepricingTriggers> repricingTriggers) {
        this.repricingTriggers = repricingTriggers;
    }

    public BigDecimal getCreditPercentage() {
        return creditPercentage;
    }

    public void setCreditPercentage(BigDecimal creditPercentage) {
        this.creditPercentage = creditPercentage;
    }

    public BigDecimal getCommercialCreditPercentage() {
        return commercialCreditPercentage;
    }

    public void setCommercialCreditPercentage(BigDecimal commercialCreditPercentage) {
        this.commercialCreditPercentage = commercialCreditPercentage;
    }

    public BigDecimal getDurbinRegulatedPercentage() {
        return durbinRegulatedPercentage;
    }

    public void setDurbinRegulatedPercentage(BigDecimal durbinRegulatedPercentage) {
        this.durbinRegulatedPercentage = durbinRegulatedPercentage;
    }
}