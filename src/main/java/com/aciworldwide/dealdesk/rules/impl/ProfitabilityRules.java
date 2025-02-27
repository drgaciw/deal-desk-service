package com.aciworldwide.dealdesk.rules.impl;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.annotation.Fact;

/**
 * Rules for calculating various profitability metrics.
 * All calculations are done using double precision for accuracy.
 */
public class ProfitabilityRules {

    @Rule(name = "Gross Revenue Calculation Rule", description = "Calculates gross revenue by subtracting net revenue from settled payment amount")
    public static class CalculateGrossRevenue {
        @Condition
        public boolean isGrossRevenueCalculationNeeded(@Fact("settledPmtAmt") double settledPmtAmt, @Fact("netRevenue") double netRevenue) {
            return settledPmtAmt > 0 && netRevenue > 0;
        }

        @Action
        public void calculateGrossRevenue(@Fact("settledPmtAmt") double settledPmtAmt, @Fact("netRevenue") double netRevenue, @Fact("grossRevenue") double[] grossRevenue) {
            grossRevenue[0] = settledPmtAmt - netRevenue;
        }
    }

    @Rule(name = "Margin Percentage Calculation Rule", description = "Calculates margin percentage as (netRevenue/grossRevenue) * 100")
    public static class CalculateMarginPercentage {
        @Condition
        public boolean isMarginPercentageCalculationNeeded(@Fact("grossRevenue") double grossRevenue, @Fact("netRevenue") double netRevenue) {
            return grossRevenue > 0 && netRevenue > 0;
        }

        @Action
        public void calculateMarginPercentage(@Fact("grossRevenue") double grossRevenue, @Fact("netRevenue") double netRevenue, @Fact("marginPercentage") double[] marginPercentage) {
            marginPercentage[0] = (netRevenue / grossRevenue) * 100;
        }
    }

    @Rule(name = "Net Revenue Per Transaction Rule", description = "Calculates average net revenue per transaction")
    public static class CalculateNetRevenuePerTransaction {
        @Condition
        public boolean isNetRevenuePerTransactionCalculationNeeded(@Fact("netRevenue") double netRevenue, @Fact("trxnCount") int trxnCount) {
            return netRevenue > 0 && trxnCount > 0;
        }

        @Action
        public void calculateNetRevenuePerTransaction(@Fact("netRevenue") double netRevenue, @Fact("trxnCount") int trxnCount, @Fact("nrPerTrans") double[] nrPerTrans) {
            nrPerTrans[0] = netRevenue / trxnCount;
        }
    }

    @Rule(name = "Uplift/Decrease Calculation Rule", description = "Calculates the difference between card and ACH net revenue")
    public static class CalculateUpliftDecrease {
        @Condition
        public boolean isUpliftDecreaseCalculationNeeded(@Fact("netRevenueCard") double netRevenueCard, @Fact("netRevenueACH") double netRevenueACH) {
            return netRevenueCard > 0 || netRevenueACH > 0;
        }

        @Action
        public void calculateUpliftDecrease(@Fact("netRevenueCard") double netRevenueCard, @Fact("netRevenueACH") double netRevenueACH, @Fact("upliftDecrease") double[] upliftDecrease) {
            upliftDecrease[0] = netRevenueCard - netRevenueACH;
        }
    }
}
