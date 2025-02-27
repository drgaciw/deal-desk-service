package com.aciworldwide.dealdesk.rules.config;

import com.aciworldwide.dealdesk.rules.core.RuleEngineListener;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.api.RulesEngineParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuleEngineConfig {

    @Bean
    public RulesEngineParameters rulesEngineParameters() {
        return new RulesEngineParameters()
            .skipOnFirstAppliedRule(false)
            .skipOnFirstFailedRule(false)
            .skipOnFirstNonTriggeredRule(false)
            .priorityThreshold(Integer.MAX_VALUE);
    }

    @Bean
    public RuleEngineListener ruleListener() {
        return new RuleEngineListener();
    }

    @Bean
    public RulesEngine rulesEngine(RulesEngineParameters parameters, RuleEngineListener listener) {
        DefaultRulesEngine engine = new DefaultRulesEngine(parameters);
        engine.registerRuleListener(listener);
        return engine;
    }
}