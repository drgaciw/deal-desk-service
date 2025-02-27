package com.aciworldwide.dealdesk.rules.api;

import com.aciworldwide.dealdesk.model.Deal;
import java.util.Collection;

public interface RuleEngine {
    void evaluateRules(Deal deal);
    void addRule(RuleDefinition rule);
    void removeRule(String ruleId);
    Collection<RuleDefinition> getRules();
    void initialize();
} 