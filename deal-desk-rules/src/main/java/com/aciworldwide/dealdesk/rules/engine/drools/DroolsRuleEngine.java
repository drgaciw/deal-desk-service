package com.aciworldwide.dealdesk.rules.engine.drools;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.rules.api.RuleEngine;
import com.aciworldwide.dealdesk.rules.api.RuleDefinition;
import com.aciworldwide.dealdesk.rules.model.Rule;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "dealdesk.rules.engine", havingValue = "drools")
public class DroolsRuleEngine implements RuleEngine {
    
    private final Map<String, RuleDefinition> ruleDefinitions;
    private KieContainer kieContainer;

    public DroolsRuleEngine() {
        this.ruleDefinitions = new HashMap<>();
    }

    @Override
    public void evaluateRules(Deal deal) {
        log.info("Evaluating rules using Drools engine for deal: {}", deal.getId());

        if (kieContainer == null) {
            log.warn("KieContainer is not initialized. No rules to evaluate.");
            return;
        }

        KieSession kieSession = kieContainer.newKieSession();
        try {
            kieSession.insert(deal);
            int firedRules = kieSession.fireAllRules();
            log.info("Fired {} rules for deal {}", firedRules, deal.getId());
        } catch (Exception e) {
            log.error("Error evaluating rules for deal {}", deal.getId(), e);
            throw new RuntimeException("Error evaluating rules", e);
        } finally {
            kieSession.dispose();
        }
    }

    @Override
    public void addRule(RuleDefinition rule) {
        ruleDefinitions.put(rule.getId(), rule);
        rebuildKieContainer();
    }

    @Override
    public void removeRule(String ruleId) {
        ruleDefinitions.remove(ruleId);
        rebuildKieContainer();
    }

    @Override
    public Collection<RuleDefinition> getRules() {
        return ruleDefinitions.values();
    }

    @Override
    public void initialize() {
        log.info("Initializing Drools Rule Engine");
        rebuildKieContainer();
    }

    private synchronized void rebuildKieContainer() {
        if (ruleDefinitions.isEmpty()) {
            kieContainer = null;
            return;
        }

        log.info("Rebuilding KieContainer with {} rules", ruleDefinitions.size());
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();

        boolean hasDroolsRules = false;
        for (RuleDefinition ruleDef : ruleDefinitions.values()) {
            if (ruleDef instanceof Rule) {
                Rule rule = (Rule) ruleDef;
                if (rule.getCondition() != null && rule.getAction() != null) {
                    String drl = generateDrl(rule);
                    kfs.write("src/main/resources/rules/" + rule.getId() + ".drl", drl);
                    hasDroolsRules = true;
                } else {
                    log.warn("Rule {} is missing condition or action, skipping Drools generation", rule.getId());
                }
            } else {
                log.warn("RuleDefinition {} is not of type Rule, cannot generate DRL", ruleDef.getId());
            }
        }

        if (!hasDroolsRules) {
            kieContainer = null;
            return;
        }

        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            String errors = kieBuilder.getResults().toString();
            log.error("Error building Drools rules: {}", errors);
            throw new RuntimeException("Build Errors:\n" + errors);
        }

        kieContainer = kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
    }

    private String generateDrl(Rule rule) {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.aciworldwide.dealdesk.rules;\n\n");
        sb.append("import com.aciworldwide.dealdesk.model.Deal;\n\n");
        sb.append("rule \"").append(rule.getName() != null ? rule.getName() : rule.getId()).append("\"\n");
        if (rule.getPriority() != 0) {
            sb.append("    salience ").append(rule.getPriority()).append("\n");
        }
        sb.append("    dialect \"mvel\"\n");
        sb.append("    when\n");
        sb.append("        deal : Deal( ").append(rule.getCondition()).append(" )\n");
        sb.append("    then\n");
        sb.append("        ").append(rule.getAction()).append(";\n");
        sb.append("end\n");
        return sb.toString();
    }
}
