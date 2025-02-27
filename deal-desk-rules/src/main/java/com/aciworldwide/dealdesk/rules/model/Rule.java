package com.aciworldwide.dealdesk.rules.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.aciworldwide.dealdesk.rules.api.RuleDefinition;

@Document(collection = "rules")
@Getter
@Setter
@Builder
public class Rule implements RuleDefinition {
    @Id
    private String id;
    private String name;
    private String description;
    private boolean active;
    private int priority;
    private String category;
    private int version;
    private boolean cacheable;
    private String condition;
    private String action;

    @Override
    public void execute(Object... facts) {
        // Base implementation - will be overridden by specific rule engine adapters
        throw new UnsupportedOperationException("Execute must be implemented by rule engine adapter");
    }
} 