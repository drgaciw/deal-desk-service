package com.aciworldwide.dealdesk.rules.engine.drools;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.rules.api.RuleEngine;
import com.aciworldwide.dealdesk.rules.api.RuleDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "dealdesk.rules.engine", havingValue = "drools")
public class DroolsRuleEngine implements RuleEngine {
    
    private final Map<String, RuleDefinition> ruleDefinitions;
    private KieBase kieBase;

    public DroolsRuleEngine() {
        this.ruleDefinitions = new HashMap<>();
    }

    @Override
    public void evaluateRules(Deal deal) {
        // Implement Drools-specific rule evaluation
        log.info("Evaluating rules using Drools engine for deal: {}", deal.getId());
    }

    @Override
    public void addRule(RuleDefinition rule) {
        ruleDefinitions.put(rule.getId(), rule);
        // Implement Drools-specific rule addition
    }

    @Override
    public void removeRule(String ruleId) {
        RuleDefinition ruleDef = ruleDefinitions.remove(ruleId);
        if (ruleDef != null) {
            if (kieBase != null) {
                String ruleName = ruleDef.getName();
                boolean removed = false;
                for (KiePackage kp : kieBase.getKiePackages()) {
                    for (Rule r : kp.getRules()) {
                        if (r.getName().equals(ruleName)) {
                            kieBase.removeRule(kp.getName(), ruleName);
                            log.debug("Removed rule {} from package {} in Drools engine", ruleName, kp.getName());
                            removed = true;
                            break;
                        }
                    }
                    if (removed) break;
                }
                if (!removed) {
                    log.warn("Rule {} not found in Drools engine, but removed from local definition", ruleName);
                }
            } else {
                log.warn("KieBase not initialized, cannot remove rule from Drools engine: {}", ruleDef.getName());
            }
        }
    }

    @Override
    public Collection<RuleDefinition> getRules() {
        return ruleDefinitions.values();
    }

    @Override
    public void initialize() {
        log.info("Initializing Drools Rule Engine");
        try {
            KieServices ks = KieServices.Factory.get();
            KieContainer kContainer = ks.getKieClasspathContainer();
            this.kieBase = kContainer.getKieBase();
        } catch (Exception e) {
            log.error("Failed to initialize Drools KieBase", e);
        }
    }
}
