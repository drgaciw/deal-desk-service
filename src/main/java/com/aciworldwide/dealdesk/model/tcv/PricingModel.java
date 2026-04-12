package com.aciworldwide.dealdesk.model.tcv;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pricing model configuration for a deal")
public class PricingModel {

    @Schema(description = "Average payment amount used in TCV calculations", example = "10000.00")
    private BigDecimal averagePayment;

    @Schema(description = "List of events that can trigger a pricing review")
    private List<RepricingTriggers> repricingTriggers;

    @Schema(description = "Credit card transaction percentage rate", example = "1.5")
    private BigDecimal creditPercentage;

    @Schema(description = "Commercial credit card transaction percentage rate", example = "2.0")
    private BigDecimal commercialCreditPercentage;

    @Schema(description = "Durbin-regulated debit transaction percentage rate", example = "0.05")
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