package com.aciworldwide.dealdesk.model.tcv;

import java.math.BigDecimal;
import java.util.List;

public class PricingModel {
    private BigDecimal averagePayment;
    private List<RepricingTriggers> repricingTriggers;
    private BigDecimal creditPercentage;
    private BigDecimal commercialCreditPercentage;
    private BigDecimal allCreditPercentage;
    private BigDecimal debitPercentage;
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

    public BigDecimal getAllCreditPercentage() {
        return allCreditPercentage;
    }

    public void setAllCreditPercentage(BigDecimal allCreditPercentage) {
        this.allCreditPercentage = allCreditPercentage;
    }

    public BigDecimal getDebitPercentage() {
        return debitPercentage;
    }

    public void setDebitPercentage(BigDecimal debitPercentage) {
        this.debitPercentage = debitPercentage;
    }

    public BigDecimal getDurbinRegulatedPercentage() {
        return durbinRegulatedPercentage;
    }

    public void setDurbinRegulatedPercentage(BigDecimal durbinRegulatedPercentage) {
        this.durbinRegulatedPercentage = durbinRegulatedPercentage;
    }
}
