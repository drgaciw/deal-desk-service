package com.aciworldwide.dealdesk.rules.config;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "rules.engine.caching")
@Data
public class RuleCacheConfig {
    private boolean enabled;
    private int maxSize;
    private int ttlMinutes;

    @Bean
    public CacheManager rulesCacheManager() {
        if (!enabled) {
            return null;
        }

        CaffeineCacheManager cacheManager = new CaffeineCacheManager("rulesCache");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
} 