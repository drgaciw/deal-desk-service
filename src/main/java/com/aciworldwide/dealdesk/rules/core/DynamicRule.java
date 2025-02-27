package com.aciworldwide.dealdesk.rules.core;

import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@Slf4j
public class DynamicRule implements Rule {
    private final String name;
    private final String description;
    private final int priority;
    private final Expression condition;
    private final Expression action;

    public DynamicRule(String name, String description, int priority, 
                      Expression condition, Expression action) {
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.condition = condition;
        this.action = action;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean evaluate(Facts facts) {
        try {
            StandardEvaluationContext context = createEvaluationContext(facts);
            return Boolean.TRUE.equals(condition.getValue(context, Boolean.class));
        } catch (Exception e) {
            log.error("Error evaluating rule condition for rule: {}", name, e);
            return false;
        }
    }

    @Override
    public void execute(Facts facts) throws Exception {
        try {
            StandardEvaluationContext context = createEvaluationContext(facts);
            action.getValue(context);
        } catch (Exception e) {
            log.error("Error executing rule action for rule: {}", name, e);
            throw e;
        }
    }

    private StandardEvaluationContext createEvaluationContext(Facts facts) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        facts.asMap().forEach(context::setVariable);
        return context;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DynamicRule other = (DynamicRule) obj;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(Rule rule) {
        if (getPriority() < rule.getPriority()) {
            return -1;
        }
        if (getPriority() > rule.getPriority()) {
            return 1;
        }
        return getName().compareTo(rule.getName());
    }
}