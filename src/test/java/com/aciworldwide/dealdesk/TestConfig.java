package com.aciworldwide.dealdesk;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import java.util.Collections;

import com.aciworldwide.dealdesk.rules.config.RuleEngineProperties;

@TestConfiguration
@EnableConfigurationProperties(RuleEngineProperties.class)
@ComponentScan(basePackages = "com.aciworldwide.dealdesk.rules.config")
public class TestConfig {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Collections.singletonList(new ConcurrentMapCache("default")));
        return cacheManager;
    }
}