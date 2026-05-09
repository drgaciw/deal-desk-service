package com.aciworldwide.dealdesk.rules.service;

import com.aciworldwide.dealdesk.rules.model.RuleDefinition;
import com.aciworldwide.dealdesk.rules.repository.RuleDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing rule definitions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleDefinitionService {

    private final RuleDefinitionRepository ruleRepository;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * Create a new rule definition.
     *
     * @param ruleDefinition the rule definition to create
     * @return the created rule definition
     */
    @Transactional
    @CachePut(value = "ruleDefinitions", key = "#result.ruleKey")
    public RuleDefinition createRule(RuleDefinition ruleDefinition) {
        validateRuleDefinition(ruleDefinition);
        
        if (ruleRepository.existsByRuleKey(ruleDefinition.getRuleKey())) {
            throw new IllegalArgumentException("Rule with key " + ruleDefinition.getRuleKey() + " already exists");
        }

        ruleDefinition.setCreatedAt(LocalDateTime.now());
        ruleDefinition.setLastModifiedAt(LocalDateTime.now());
        
        return ruleRepository.save(ruleDefinition);
    }

    /**
     * Update an existing rule definition.
     *
     * @param ruleKey the key of the rule to update
     * @param ruleDefinition the updated rule definition
     * @return the updated rule definition
     */
    @Transactional
    @CachePut(value = "ruleDefinitions", key = "#ruleKey")
    public RuleDefinition updateRule(String ruleKey, RuleDefinition ruleDefinition) {
        validateRuleDefinition(ruleDefinition);
        
        RuleDefinition existingRule = ruleRepository.findByRuleKey(ruleKey)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found with key: " + ruleKey));

        // Update fields
        existingRule.setName(ruleDefinition.getName());
        existingRule.setDescription(ruleDefinition.getDescription());
        existingRule.setCategory(ruleDefinition.getCategory());
        existingRule.setPriority(ruleDefinition.getPriority());
        existingRule.setConditionExpression(ruleDefinition.getConditionExpression());
        existingRule.setActionExpression(ruleDefinition.getActionExpression());
        existingRule.setEnabled(ruleDefinition.isEnabled());
        existingRule.setParameters(ruleDefinition.getParameters());
        existingRule.setValidFrom(ruleDefinition.getValidFrom());
        existingRule.setValidTo(ruleDefinition.getValidTo());
        existingRule.setLastModifiedAt(LocalDateTime.now());
        existingRule.setLastModifiedBy(ruleDefinition.getLastModifiedBy());

        return ruleRepository.save(existingRule);
    }

    /**
     * Delete a rule definition.
     *
     * @param ruleKey the key of the rule to delete
     */
    @Transactional
    @CacheEvict(value = "ruleDefinitions", key = "#ruleKey")
    public void deleteRule(String ruleKey) {
        RuleDefinition rule = ruleRepository.findByRuleKey(ruleKey)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found with key: " + ruleKey));
        ruleRepository.delete(rule);
    }

    /**
     * Get a rule definition by key.
     *
     * @param ruleKey the key of the rule to get
     * @return the rule definition
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "ruleDefinitions", key = "#ruleKey")
    public Optional<RuleDefinition> getRule(String ruleKey) {
        return ruleRepository.findByRuleKey(ruleKey);
    }

    /**
     * Get all active rules for a category.
     *
     * @param category the category to get rules for
     * @return the list of active rules
     */
    @Transactional(readOnly = true)
    public List<RuleDefinition> getActiveRules(String category) {
        return ruleRepository.findActiveRulesByCategory(category, LocalDateTime.now());
    }

    /**
     * Get all rules for a category.
     *
     * @param category the category to get rules for
     * @return the list of rules
     */
    @Transactional(readOnly = true)
    public List<RuleDefinition> getRulesByCategory(String category) {
        return ruleRepository.findByCategory(category);
    }

    /**
     * Get all active rule categories.
     *
     * @return the list of active categories
     */
    @Transactional(readOnly = true)
    public List<String> getActiveCategories() {
        return ruleRepository.findAllActiveCategories();
    }

    /**
     * Validate a rule definition.
     *
     * @param ruleDefinition the rule definition to validate
     * @throws IllegalArgumentException if the rule definition is invalid
     */
    private void validateRuleDefinition(RuleDefinition ruleDefinition) {
        if (ruleDefinition.getRuleKey() == null || ruleDefinition.getRuleKey().trim().isEmpty()) {
            throw new IllegalArgumentException("Rule key is required");
        }
        if (ruleDefinition.getName() == null || ruleDefinition.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Rule name is required");
        }
        if (ruleDefinition.getCategory() == null || ruleDefinition.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Rule category is required");
        }
        if (ruleDefinition.getConditionExpression() == null || ruleDefinition.getConditionExpression().trim().isEmpty()) {
            throw new IllegalArgumentException("Rule condition expression is required");
        }
        if (ruleDefinition.getActionExpression() == null || ruleDefinition.getActionExpression().trim().isEmpty()) {
            throw new IllegalArgumentException("Rule action expression is required");
        }

        // Validate expressions
        try {
            expressionParser.parseExpression(ruleDefinition.getConditionExpression());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid condition expression: " + e.getMessage());
        }

        try {
            expressionParser.parseExpression(ruleDefinition.getActionExpression());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid action expression: " + e.getMessage());
        }

        // Validate dates
        if (ruleDefinition.getValidFrom() == null) {
            throw new IllegalArgumentException("Valid from date is required");
        }
        if (ruleDefinition.getValidTo() != null && ruleDefinition.getValidTo().isBefore(ruleDefinition.getValidFrom())) {
            throw new IllegalArgumentException("Valid to date must be after valid from date");
        }
    }
}