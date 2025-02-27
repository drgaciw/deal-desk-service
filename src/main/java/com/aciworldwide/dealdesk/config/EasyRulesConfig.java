package com.aciworldwide.dealdesk.config;

import org.jeasy.rules.api.Rules;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EasyRulesConfig {

    @Bean
    public Rules rules() {
        return new Rules();
    }
} 