package com.aciworldwide.dealdesk.rules.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(RuleEnginePropertiesTest.TestConfig.class)
class RuleEnginePropertiesTest {

    @TestConfiguration
    @EnableConfigurationProperties
    static class TestConfig {
        @Bean
        RuleEngineProperties ruleEngineProperties() {
            RuleEngineProperties properties = new RuleEngineProperties();
            properties.getAudit().setEnabled(true);
            properties.getAudit().setRetentionDays(30);
            return properties;
        }
    }

    @Autowired
    private RuleEngineProperties properties;

    @Test
    void shouldLoadDefaultProperties() {
        assertThat(properties).isNotNull();
        assertThat(properties.getAudit().isEnabled()).isTrue();
        assertThat(properties.getAudit().getRetentionDays()).isEqualTo(30);
    }
}