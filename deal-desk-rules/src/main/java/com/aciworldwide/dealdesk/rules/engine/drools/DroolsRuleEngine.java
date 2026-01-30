package com.aciworldwide.dealdesk.rules.engine.drools;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.rules.api.RuleDefinition;
import com.aciworldwide.dealdesk.rules.api.RuleEngine;
import com.aciworldwide.dealdesk.rules.model.Rule;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "dealdesk.rules.engine", havingValue = "drools")
public class DroolsRuleEngine implements RuleEngine {

    private static final Logger log = LoggerFactory.getLogger(DroolsRuleEngine.class);

    private final Map<String, RuleDefinition> ruleDefinitions;
    private KieServices kieServices;
    private KieFileSystem kieFileSystem;
    private volatile KieContainer kieContainer;

    public DroolsRuleEngine() {
        this.ruleDefinitions = new ConcurrentHashMap<>();
    }

    @Override
    public void initialize() {
        log.info("Initializing Drools Rule Engine");
        this.kieServices = KieServices.Factory.get();
        this.kieFileSystem = kieServices.newKieFileSystem();
        // Initialize with empty container or load existing rules if any
        buildKieContainer();
    }

    private synchronized void buildKieContainer() {
        if (kieServices == null) return;

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            String errorMsg = "Error building Drools rules: " + kieBuilder.getResults().toString();
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        KieRepository kieRepository = kieServices.getRepository();
        this.kieContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
    }

    @Override
    public void evaluateRules(Deal deal) {
        KieContainer currentContainer = this.kieContainer;
        if (currentContainer == null) {
            log.warn("KieContainer is not initialized. Skipping rule evaluation.");
            return;
        }
        log.info("Evaluating rules using Drools engine for deal: {}", deal.getId());
        KieSession kieSession = currentContainer.newKieSession();
        try {
            kieSession.insert(deal);
            kieSession.fireAllRules();
        } catch (Exception e) {
            log.error("Error evaluating rules", e);
        } finally {
            kieSession.dispose();
        }
    }

    @Override
    public synchronized void addRule(RuleDefinition rule) {
        ruleDefinitions.put(rule.getId(), rule);

        if (rule instanceof Rule) {
            Rule concreteRule = (Rule) rule;
            String drl = generateDrl(concreteRule);
            String drlPath = "src/main/resources/com/aciworldwide/dealdesk/rules/" + rule.getId() + ".drl";

            if (kieFileSystem == null) {
                 if (kieServices == null) {
                     this.kieServices = KieServices.Factory.get();
                     this.kieFileSystem = kieServices.newKieFileSystem();
                 }
            }

            kieFileSystem.write(drlPath, drl);

            // Rebuild container to include new rule
            buildKieContainer();
        } else {
            log.warn("Rule {} is not an instance of com.aciworldwide.dealdesk.rules.model.Rule, skipping Drools addition.", rule.getId());
        }
    }

    @Override
    public synchronized void removeRule(String ruleId) {
        ruleDefinitions.remove(ruleId);
        if (kieFileSystem != null) {
            String drlPath = "src/main/resources/com/aciworldwide/dealdesk/rules/" + ruleId + ".drl";
            kieFileSystem.delete(drlPath);
            buildKieContainer();
        }
    }

    @Override
    public Collection<RuleDefinition> getRules() {
        return ruleDefinitions.values();
    }

    private String generateDrl(Rule rule) {
        StringBuilder drl = new StringBuilder();
        drl.append("package com.aciworldwide.dealdesk.rules;\n\n");
        drl.append("import com.aciworldwide.dealdesk.model.Deal;\n\n");
        drl.append("rule \"").append(rule.getName() != null ? rule.getName() : rule.getId()).append("\"\n");
        drl.append("    dialect \"java\"\n");
        drl.append("    when\n");
        drl.append("        $deal : Deal( ").append(rule.getCondition()).append(" )\n");
        drl.append("    then\n");
        drl.append("        ").append(rule.getAction()).append("\n");
        drl.append("end\n");
        return drl.toString();
    }
}
