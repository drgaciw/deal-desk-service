package com.aciworldwide.dealdesk.rules.cache;

import java.util.Optional;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import com.aciworldwide.dealdesk.rules.model.DealRule;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RuleCache {
    private final CacheManager rulesCacheManager;
    private static final String CACHE_NAME = "rulesCache";

    public Optional<DealRule> get(String ruleId) {
        return Optional.ofNullable(rulesCacheManager.getCache(CACHE_NAME))
                .map(cache -> cache.get(ruleId, DealRule.class));
    }

    public void put(String ruleId, DealRule rule) {
        Optional.ofNullable(rulesCacheManager.getCache(CACHE_NAME))
                .ifPresent(cache -> cache.put(ruleId, rule));
    }

    public void evict(String ruleId) {
        Optional.ofNullable(rulesCacheManager.getCache(CACHE_NAME))
                .ifPresent(cache -> cache.evict(ruleId));
    }

    public void clear() {
        Optional.ofNullable(rulesCacheManager.getCache(CACHE_NAME))
                .ifPresent(cache -> cache.clear());
    }
} 