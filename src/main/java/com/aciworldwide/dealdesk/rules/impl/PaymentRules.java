package com.aciworldwide.dealdesk.rules.impl;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.annotation.Fact;

public class PaymentRules {

    @Rule
    public static class ComCreditRule {
        @Condition
        public boolean isComCreditPayment(@Fact("paymentType") String paymentType) {
            return "Com Credit".equals(paymentType);
        }

        @Action
        public void calculateFee(@Fact("principal") double principal, @Fact("totalFee") double[] totalFee) {
            totalFee[0] = principal * 0.0295; // 2.95% of the principal
        }
    }

    @Rule
    public static class ConsCreditRule {
        @Condition
        public boolean isConsCreditPayment(@Fact("paymentType") String paymentType) {
            return "Cons Credit".equals(paymentType);
        }

        @Action
        public void calculateFee(@Fact("totalFee") double[] totalFee) {
            totalFee[0] = 2.00; // Flat fee of $2.00
        }
    }

    @Rule
    public static class ATMPinlessRule {
        @Condition
        public boolean isATMPinlessPayment(@Fact("paymentType") String paymentType) {
            return "ATM/Pinless".equals(paymentType);
        }

        @Action
        public void calculateFee(@Fact("cost") double cost, @Fact("totalFee") double[] totalFee) {
            totalFee[0] = cost + 7.00; // Cost + $7.00
        }
    }

    @Rule
    public static class DebitRule {
        @Condition
        public boolean isDebitPayment(@Fact("paymentType") String paymentType) {
            return "Debit".equals(paymentType);
        }

        @Action
        public void calculateFee(@Fact("cost") double cost, @Fact("principal") double principal, @Fact("totalFee") double[] totalFee) {
            totalFee[0] = cost + (principal * 0.0175); // Cost + 1.75% of the principal
        }
    }

    @Rule
    public static class ACHRule {
        @Condition
        public boolean isACHPayment(@Fact("paymentType") String paymentType) {
            return "ACH".equals(paymentType);
        }

        @Action
        public void calculateFee(@Fact("totalFee") double[] totalFee) {
            totalFee[0] = 0.05; // Flat fee of $0.05
        }
    }
}
