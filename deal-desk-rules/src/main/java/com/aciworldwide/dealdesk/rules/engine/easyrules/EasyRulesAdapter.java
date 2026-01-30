package com.aciworldwide.dealdesk.rules.engine.easyrules;

import com.aciworldwide.dealdesk.rules.api.RuleDefinition;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;

@Rule(name = "EasyRulesAdapter")
public class EasyRulesAdapter implements org.jeasy.rules.api.Rule {
    
    private final RuleDefinition ruleDefinition;

    public EasyRulesAdapter(RuleDefinition ruleDefinition) {
        this.ruleDefinition = ruleDefinition;
    }

    @Override
    public boolean evaluate(Facts facts) {
        return ruleDefinition.isActive();
    }

    @Override
    public void execute(Facts facts) throws Exception {
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

    @Override
    public int compareTo(org.jeasy.rules.api.Rule other) {
        return Integer.compare(this.getPriority(), other.getPriority());
    }
} 