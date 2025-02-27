package com.aciworldwide.dealdesk.rules.engine.easyrules;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.rules.api.RuleEngine;
import com.aciworldwide.dealdesk.rules.api.RuleDefinition;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "dealdesk.rules.engine", havingValue = "easyrules")
public class EasyRulesEngine implements RuleEngine {
    
    private final RulesEngine rulesEngine;
    private final Map<String, RuleDefinition> ruleDefinitions;
    private final Rules rules;

    public EasyRulesEngine() {
        this.rulesEngine = new DefaultRulesEngine();
        this.ruleDefinitions = new HashMap<>();
        this.rules = new Rules();
    }

    @Override
    public void evaluateRules(Deal deal) {
        Facts facts = new Facts();
        facts.put("deal", deal);
        rulesEngine.fire(rules, facts);
    }

    @Override
    public void addRule(RuleDefinition rule) {
        EasyRulesAdapter adapter = new EasyRulesAdapter(rule);
        ruleDefinitions.put(rule.getId(), rule);
        rules.register(adapter);
    }

    @Override
    public void removeRule(String ruleId) {
        RuleDefinition rule = ruleDefinitions.remove(ruleId);
        if (rule != null) {
            rules.unregister(rule);
        }
    }

    @Override
    public Collection<RuleDefinition> getRules() {
        return ruleDefinitions.values();
    }

    @Override
    public void initialize() {
        log.info("Initializing Easy Rules Engine");
    }
} 