package com.aciworldwide.dealdesk.rules.service;

import java.util.List;

import com.aciworldwide.dealdesk.model.Deal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.aciworldwide.dealdesk.rules.exception.RuleNotFoundException;
import com.aciworldwide.dealdesk.rules.exception.RuleValidationException;
import com.aciworldwide.dealdesk.rules.model.DealRule;
import com.aciworldwide.dealdesk.rules.model.RuleVersion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleService {
    private final RuleCachingService ruleCachingService;
    private final RuleVersioningService versioningService;

    public DealRule getRuleById(String ruleId) {
        Assert.hasText(ruleId, "Rule ID cannot be empty");
        log.debug("Fetching rule with ID: {}", ruleId);
        
        DealRule rule = ruleCachingService.getRule(ruleId);
        if (rule == null) {
            log.error("Rule not found with ID: {}", ruleId);
            throw new RuleNotFoundException("Rule not found: " + ruleId);
        }
        return rule;
    }

    @Transactional
    public void updateRule(DealRule rule, String modifiedBy, String changeDescription) {
        validateRuleUpdate(rule, modifiedBy, changeDescription);
        
        try {
            log.debug("Updating rule: {} ({})", rule.getName(), rule.getId());
            versioningService.createVersion(rule, modifiedBy, changeDescription);
            log.info("Rule updated successfully: {}", rule.getId());
        } catch (Exception e) {
            log.error("Failed to update rule {}: {}", rule.getId(), e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void deleteRule(String ruleId) {
        Assert.hasText(ruleId, "Rule ID cannot be empty");
        
        try {
            log.debug("Deleting rule: {}", ruleId);
            ruleCachingService.invalidateRule(ruleId);
            log.info("Rule deleted successfully: {}", ruleId);
        } catch (Exception e) {
            log.error("Failed to delete rule {}: {}", ruleId, e.getMessage());
            throw e;
        }
    }
    
    public List<RuleVersion> getRuleVersionHistory(String ruleId) {
        Assert.hasText(ruleId, "Rule ID cannot be empty");
        log.debug("Fetching version history for rule: {}", ruleId);
        
        try {
            return versioningService.getVersionHistory(ruleId);
        } catch (Exception e) {
            log.error("Failed to fetch version history for rule {}: {}", ruleId, e.getMessage());
            throw e;
        }
    }
    
    @Transactional
    public DealRule rollbackRule(String ruleId, int version) {
        Assert.hasText(ruleId, "Rule ID cannot be empty");
        Assert.isTrue(version > 0, "Version must be greater than 0");
        
        try {
            log.debug("Rolling back rule {} to version {}", ruleId, version);
            DealRule rolledBackRule = versioningService.rollbackToVersion(ruleId, version);
            log.info("Rule {} rolled back to version {} successfully", ruleId, version);
            return rolledBackRule;
        } catch (Exception e) {
            log.error("Failed to rollback rule {} to version {}: {}", ruleId, version, e.getMessage());
            throw e;
        }
    }

    public DealRule createRule(DealRule rule) {
        validateNewRule(rule);
        
        try {
            log.debug("Creating new rule: {}", rule.getName());
            DealRule savedRule = ruleCachingService.saveRule(rule);
            log.info("Rule created successfully: {}", savedRule.getId());
            return savedRule;
        } catch (Exception e) {
            log.error("Failed to create rule {}: {}", rule.getName(), e.getMessage());
            throw e;
        }
    }

    private void validateRuleUpdate(DealRule rule, String modifiedBy, String changeDescription) {
        Assert.notNull(rule, "Rule cannot be null");
        Assert.hasText(rule.getId(), "Rule ID cannot be empty");
        Assert.hasText(modifiedBy, "Modified by cannot be empty");
        Assert.hasText(changeDescription, "Change description cannot be empty");
        
        if (!rule.isActive() && rule.isRequired()) {
            throw new RuleValidationException("Cannot deactivate required rule: " + rule.getId());
        }
    }

    private void validateNewRule(DealRule rule) {
        Assert.notNull(rule, "Rule cannot be null");
        Assert.hasText(rule.getName(), "Rule name cannot be empty");
        
        if (rule.isRequired() && !rule.isActive()) {
            throw new RuleValidationException("Required rule must be active");
        }
    }

    public Deal evaluateRules(Deal deal) {
        Assert.notNull(deal, "Deal cannot be null");
        log.debug("Evaluating rules for deal: {}", deal.getId());
        try {
            // Apply rules to the deal
            // ...
            log.info("Rules evaluated successfully for deal: {}", deal.getId());
            return deal;
        } catch (Exception e) {
            log.error("Failed to evaluate rules for deal {}: {}", deal.getId(), e.getMessage());
            throw e;
        }
    }
}