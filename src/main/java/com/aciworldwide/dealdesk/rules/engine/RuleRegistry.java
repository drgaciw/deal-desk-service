package com.aciworldwide.dealdesk.rules.engine;

import org.jeasy.rules.api.Rules;
import org.springframework.stereotype.Component;

import com.aciworldwide.dealdesk.rules.impl.CreditPercentageRules;
import com.aciworldwide.dealdesk.rules.impl.DebitRegulationRules;
import com.aciworldwide.dealdesk.rules.impl.PaymentThresholdRule;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RuleRegistry {
    private final PaymentThresholdRule paymentThresholdRule;
    private final CreditPercentageRules creditPercentageRules;
    private final DebitRegulationRules.DebitPercentageRule debitPercentageRule;
    private final DebitRegulationRules.DurbinRegulatedRule durbinRegulatedRule;

    private Rules rules;

    @PostConstruct
    public void initialize() {
        // Register all rules with jEasy Rules
        rules = new Rules();
        rules.register(paymentThresholdRule);
        rules.register(creditPercentageRules);
        rules.register(debitPercentageRule);
        rules.register(durbinRegulatedRule);
    }
}