package com.aciworldwide.dealdesk.rules.engine.drools;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.rules.api.RuleEngine;
import com.aciworldwide.dealdesk.rules.api.RuleDefinition;
import com.aciworldwide.dealdesk.rules.model.Rule;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ConditionalOnProperty(name = "dealdesk.rules.engine", havingValue = "drools")
public class DroolsRuleEngine implements RuleEngine {
    
    private final Map<String, RuleDefinition> ruleDefinitions;
    private KieBase kieBase;

    public DroolsRuleEngine() {
        this.ruleDefinitions = new ConcurrentHashMap<>();
    }

    @Override
    public void evaluateRules(Deal deal) {
        if (kieBase == null) {
            log.warn("Drools KieBase not initialized. Skipping evaluation for deal: {}", deal.getId());
            return;
        }
        log.info("Evaluating rules using Drools engine for deal: {}", deal.getId());
        // To implement evaluation, we would need a KieSession.
        // This task focuses on rule addition, so we leave this log.
    }

    @Override
    public void addRule(RuleDefinition ruleDefinition) {
        if (!(ruleDefinition instanceof Rule)) {
            log.warn("Rule definition is not of type Rule, cannot add to Drools engine: {}", ruleDefinition.getId());
            return;
        }
        Rule rule = (Rule) ruleDefinition;
        ruleDefinitions.put(rule.getId(), rule);
        rebuildKieBase();
    }

    @Override
    public void removeRule(String ruleId) {
        if (ruleDefinitions.remove(ruleId) != null) {
            rebuildKieBase();
        } else {
             log.warn("Rule with ID {} not found, cannot remove.", ruleId);
        }
    }

    @Override
    public Collection<RuleDefinition> getRules() {
        return ruleDefinitions.values();
    }

    @Override
    public void initialize() {
        log.info("Initializing Drools Rule Engine");
        rebuildKieBase();
    }

    private synchronized void rebuildKieBase() {
        log.info("Rebuilding Drools KieBase with {} rules", ruleDefinitions.size());
        try {
            KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();

            for (RuleDefinition def : ruleDefinitions.values()) {
                if (def instanceof Rule) {
                    Rule rule = (Rule) def;
                    String drl = generateDrl(rule);
                    kfs.write("src/main/resources/com/aciworldwide/dealdesk/rules/" + rule.getId() + ".drl", drl);
                }
            }

            KieBuilder kieBuilder = ks.newKieBuilder(kfs);
            kieBuilder.buildAll();

            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                log.error("Drools build errors: {}", kieBuilder.getResults().toString());
                // We might not want to throw exception here to avoid crashing the app, but logging is critical
                return;
            }

            KieContainer kContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
            this.kieBase = kContainer.getKieBase();
            log.info("Drools KieBase rebuilt successfully.");

        } catch (Exception e) {
            log.error("Failed to rebuild Drools KieBase", e);
        }
    }

    private String generateDrl(Rule rule) {
        StringBuilder drl = new StringBuilder();
        drl.append("package com.aciworldwide.dealdesk.rules;\n\n");
        drl.append("import com.aciworldwide.dealdesk.model.Deal;\n");
        drl.append("import com.aciworldwide.dealdesk.model.DealStatus;\n\n");

        drl.append("rule \"").append(rule.getName()).append("\"\n");
        drl.append("    dialect \"mvel\"\n");
        drl.append("    when\n");
        drl.append("        ").append(rule.getCondition()).append("\n");
        drl.append("    then\n");
        drl.append("        ").append(rule.getAction()).append("\n");
        drl.append("end\n");

        return drl.toString();
    }
}
