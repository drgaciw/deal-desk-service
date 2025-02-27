package com.aciworldwide.dealdesk.rules.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "dealdesk.rules")
@Data
public class RuleEngineConfig {
    private String engine = "easyrules"; // default value
} 