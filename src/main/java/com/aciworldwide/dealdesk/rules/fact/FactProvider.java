package com.aciworldwide.dealdesk.rules.fact;

import org.jeasy.rules.api.Facts;

/**
 * Interface for providers that prepare facts for rule execution.
 */
public interface FactProvider {
    /**
     * Get the type of context this provider supports
     *
     * @return The class type this provider supports
     */
    Class<?> getSupportedContextType();

    /**
     * Provide facts into the facts collection based on context
     *
     * @param context The context object containing data for fact creation
     * @param facts The facts collection to populate
     */
    void provideFacts(Object context, Facts facts);
}