package com.aciworldwide.dealdesk.rules.service;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.rules.api.RuleEngine;
import com.aciworldwide.dealdesk.rules.api.RuleDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleExecutionService {
    private final RuleEngine ruleEngine;

    @PostConstruct
    public void init() {
        ruleEngine.initialize();
    }

    public void processDeal(Deal deal) {
        log.info("Processing deal with ID: {}", deal.getId());
        ruleEngine.evaluateRules(deal);
    }

    public void addRule(RuleDefinition rule) {
        ruleEngine.addRule(rule);
    }

    public void removeRule(String ruleId) {
        ruleEngine.removeRule(ruleId);
    }

    public Collection<RuleDefinition> getRules() {
        return ruleEngine.getRules();
    }
} 