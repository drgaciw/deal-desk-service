package com.aciworldwide.dealdesk.rules.engine.easyrules;

import com.aciworldwide.dealdesk.rules.api.RuleDefinition;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.annotation.Rule;

@Rule(name = "EasyRulesAdapter")
public class EasyRulesAdapter implements org.jeasy.rules.api.Rule {
    
    private final RuleDefinition ruleDefinition;

    public EasyRulesAdapter(RuleDefinition ruleDefinition) {
        this.ruleDefinition = ruleDefinition;
    }

    @Condition
    public boolean evaluate(Object... facts) {
        return ruleDefinition.isActive();
    }

    @Action
    public void execute(Object... facts) {
        ruleDefinition.execute(facts);
    }

    @Priority
    public int getPriority() {
        return ruleDefinition.getPriority();
    }

    @Override
    public String getName() {
        return ruleDefinition.getName();
    }

    @Override
    public String getDescription() {
        return ruleDefinition.getDescription();
    }
} 