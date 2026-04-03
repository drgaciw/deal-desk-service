package com.aciworldwide.dealdesk.rules.service;

import com.aciworldwide.dealdesk.rules.core.DynamicRule;
import com.aciworldwide.dealdesk.rules.fact.FactProvider;
import com.aciworldwide.dealdesk.rules.model.RuleDefinition;
import com.aciworldwide.dealdesk.rules.repository.RuleDefinitionRepository;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.RulesEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleExecutionService {

    private final RulesEngine rulesEngine;
    private final RuleDefinitionRepository ruleRepository;
    private final List<FactProvider> factProviders;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final Map<String, Expression> compiledExpressions = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public Facts executeRules(String category, Object context) {
        log.debug("Executing rules for category: {} with context type: {}", 
                category, context.getClass().getSimpleName());

        Facts facts = new Facts();
        
        // Prepare facts from all relevant providers
        factProviders.stream()
            .filter(provider -> provider.getSupportedContextType().isInstance(context))
            .forEach(provider -> provider.provideFacts(context, facts));

        // Load active rules for the category
        List<RuleDefinition> ruleDefinitions = ruleRepository
            .findActiveRulesByCategory(category, LocalDateTime.now());

        if(ruleDefinitions.isEmpty()) {
            return facts;
        }

        // Convert rule definitions to executable rules
        RuleContainer rules = new RuleContainer();
        ruleDefinitions.forEach(definition -> 
            rules.register(createRule(definition)));

        // Execute rules only if there are any registered
        if (!rules.getRules().isEmpty()) {
            rulesEngine.fire(rules, facts);
        }

        return facts;
    }

    private Rule createRule(RuleDefinition definition) {
        Expression condition = getOrCompileExpression(definition.getConditionExpression());
        Expression action = getOrCompileExpression(definition.getActionExpression());
        
        return new DynamicRule(
            definition.getName(),
            definition.getDescription(),
            definition.getPriority(),
            condition,
            action
        );
    }

    private Expression getOrCompileExpression(String expressionString) {
        return compiledExpressions.computeIfAbsent(expressionString,
            expr -> expressionParser.parseExpression(expr));
    }

    // New method to retrieve and return a list of rules
    @org.springframework.cache.annotation.Cacheable("rules")
    public List<Rule> getRules() {
        // Assuming ruleRepository.findAll() retrieves all rule definitions
        List<RuleDefinition> ruleDefinitions = ruleRepository.findAll();
        RuleContainer ruleContainer = new RuleContainer();
        for (RuleDefinition definition : ruleDefinitions) {
            ruleContainer.register(createRule(definition));
        }
        return ruleContainer.getRules();
    }
}