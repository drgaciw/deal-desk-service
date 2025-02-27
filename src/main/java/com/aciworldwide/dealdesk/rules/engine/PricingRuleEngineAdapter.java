package com.aciworldwide.dealdesk.rules.engine;

import com.aciworldwide.dealdesk.model.Deal;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.springframework.stereotype.Component;

@Component
public class PricingRuleEngineAdapter implements PricingRuleEngine {

    private final RulesEngine rulesEngine;
    private final Rules pricingRules;

    public PricingRuleEngineAdapter(RulesEngine rulesEngine, Rules pricingRules) {
        this.rulesEngine = rulesEngine;
        this.pricingRules = pricingRules;
    }

    @Override
    public void evaluateRules(Deal deal) {
        Facts facts = new Facts();
        facts.put("deal", deal);
        rulesEngine.fire(pricingRules, facts);
    }

    @Override
    public void execute(Rules rules, Facts facts) {
        rulesEngine.fire(rules, facts);
    }
}