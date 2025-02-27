package com.aciworldwide.dealdesk.rules.api;

public interface RuleDefinition {
    String getId();
    String getName();
    String getDescription();
    int getPriority();
    boolean isActive();
    String getCategory();
    int getVersion();
    boolean isCacheable();
    void execute(Object... facts);
} 