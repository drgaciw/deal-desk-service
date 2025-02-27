package com.aciworldwide.dealdesk.rules.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@EnableConfigurationProperties(RuleEngineProperties.class)
public class TestConfig {
    
    @Bean
    RuleEngineProperties ruleEngineProperties() {
        return new RuleEngineProperties();
    }
} 