package com.aciworldwide.dealdesk.rules.config;

import com.aciworldwide.dealdesk.rules.core.RuleEngineListener;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.api.RulesEngineParameters;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestRuleEngineConfig {

    @Bean
    @Primary
    public RulesEngineParameters testRulesEngineParameters() {
        return new RulesEngineParameters()
            .skipOnFirstAppliedRule(false)
            .skipOnFirstFailedRule(false)
            .skipOnFirstNonTriggeredRule(false)
            .priorityThreshold(Integer.MAX_VALUE);
    }

    @Bean
    @Primary
    public RuleEngineListener testRuleListener() {
        return new RuleEngineListener();
    }

    @Bean
    @Primary
    public RulesEngine testRulesEngine(RulesEngineParameters parameters, RuleEngineListener listener) {
        DefaultRulesEngine engine = new DefaultRulesEngine(parameters);
        engine.registerRuleListener(listener);
        return engine;
    }
}