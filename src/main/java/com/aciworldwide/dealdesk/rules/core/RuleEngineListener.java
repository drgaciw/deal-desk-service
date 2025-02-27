package com.aciworldwide.dealdesk.rules.core;

import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.RuleListener;

@Slf4j
public class RuleEngineListener implements RuleListener {

    @Override
    public boolean beforeEvaluate(Rule rule, Facts facts) {
        log.debug("Evaluating rule '{}' with priority {}", rule.getName(), rule.getPriority());
        return true;
    }

    @Override
    public void afterEvaluate(Rule rule, Facts facts, boolean evaluationResult) {
        if (log.isDebugEnabled()) {
            log.debug("Rule '{}' evaluated to {}", rule.getName(), evaluationResult);
        }
    }

    @Override
    public void beforeExecute(Rule rule, Facts facts) {
        log.debug("Executing rule '{}'", rule.getName());
    }

    @Override
    public void onSuccess(Rule rule, Facts facts) {
        log.info("Rule '{}' executed successfully", rule.getName());
    }

    @Override
    public void onFailure(Rule rule, Facts facts, Exception exception) {
        log.error("Rule '{}' execution failed", rule.getName(), exception);
    }
}