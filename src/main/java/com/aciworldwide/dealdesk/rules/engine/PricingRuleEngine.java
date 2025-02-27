package com.aciworldwide.dealdesk.rules.engine;

import com.aciworldwide.dealdesk.model.Deal;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;

public interface PricingRuleEngine {
    void evaluateRules(Deal deal);
    void execute(Rules rules, Facts facts);
}