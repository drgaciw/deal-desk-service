package com.aciworldwide.dealdesk.rules.service;

import org.springframework.stereotype.Service;

import com.aciworldwide.dealdesk.rules.cache.RuleCache;
import com.aciworldwide.dealdesk.rules.exception.RuleNotFoundException;
import com.aciworldwide.dealdesk.rules.model.DealRule;
import com.aciworldwide.dealdesk.rules.repository.RuleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleCachingService {
    private final RuleCache ruleCache;
    private final RuleRepository ruleRepository;

    public DealRule getRule(String ruleId) {
        log.debug("Fetching rule with ID: {}", ruleId);
        return ruleCache.get(ruleId)
                .orElseGet(() -> loadAndCacheRule(ruleId));
    }

    private DealRule loadAndCacheRule(String ruleId) {
        log.debug("Cache miss for rule: {}, loading from repository", ruleId);
        try {
            return ruleRepository.findById(ruleId)
                    .map(this::cacheRule)
                    .orElseThrow(() -> {
                        log.error("Rule not found with ID: {}", ruleId);
                        return new RuleNotFoundException("Rule not found: " + ruleId);
                    });
        } catch (Exception e) {
            log.error("Error loading rule {}: {}", ruleId, e.getMessage());
            throw e;
        }
    }

    private DealRule cacheRule(DealRule rule) {
        try {
            log.debug("Caching rule: {} ({})", rule.getName(), rule.getId());
            ruleCache.put(rule.getId(), rule);
            return rule;
        } catch (Exception e) {
            log.warn("Failed to cache rule {}: {}", rule.getId(), e.getMessage());
            return rule; // Return rule even if caching fails
        }
    }

    public void invalidateRule(String ruleId) {
        try {
            log.debug("Invalidating cached rule: {}", ruleId);
            ruleCache.evict(ruleId);
        } catch (Exception e) {
            log.warn("Failed to invalidate rule {}: {}", ruleId, e.getMessage());
        }
    }

    public void invalidateAllRules() {
        try {
            log.debug("Invalidating all cached rules");
            ruleCache.clear();
        } catch (Exception e) {
            log.error("Failed to clear rule cache: {}", e.getMessage());
            throw e;
        }
    }

    public DealRule saveRule(DealRule rule) {
        try {
            log.debug("Saving rule: {} ({})", rule.getName(), rule.getId());
            DealRule savedRule = ruleRepository.save(rule);
            return cacheRule(savedRule);
        } catch (Exception e) {
            log.error("Failed to save rule {}: {}", rule.getId(), e.getMessage());
            throw e;
        }
    }
} 