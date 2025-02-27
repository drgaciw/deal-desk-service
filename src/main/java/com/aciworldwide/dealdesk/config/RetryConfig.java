package com.aciworldwide.dealdesk.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import com.aciworldwide.dealdesk.exception.SalesforceIntegrationException;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for Spring Retry functionality.
 * Provides retry capabilities for transient failures in external service calls.
 * 
 * <p>Features:
 * - Exponential backoff with configurable parameters
 * - Retry policy with configurable max attempts
 * - Exception-based retry targeting
 * - Customizable retry behavior per exception type
 */
@Configuration
@EnableRetry
@Slf4j
public class RetryConfig {
    
    private static final long INITIAL_INTERVAL = 1000L; // 1 second
    private static final double MULTIPLIER = 2.0;
    private static final long MAX_INTERVAL = 10000L; // 10 seconds
    private static final int MAX_ATTEMPTS = 3;

    /**
     * Creates a RetryTemplate bean with exponential backoff policy.
     * Only created if no other RetryTemplate bean exists.
     *
     * @return configured RetryTemplate for handling retryable operations
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryTemplate retryTemplate() {
        log.debug("Creating retry template with exponential backoff");
        
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Configure exponential backoff
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(INITIAL_INTERVAL);    // Start with 1 second delay
        backOffPolicy.setMultiplier(MULTIPLIER);              // Double the delay each retry
        backOffPolicy.setMaxInterval(MAX_INTERVAL);           // Cap at 10 seconds
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // Configure retry policy with exception handling
        retryTemplate.setRetryPolicy(createRetryPolicy());
        
        return retryTemplate;
    }

    /**
     * Creates a retry policy with specific exception handling rules.
     * 
     * @return configured RetryPolicy
     */
    private RetryPolicy createRetryPolicy() {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        
        // Configure which exceptions should trigger retry
        retryableExceptions.put(SalesforceIntegrationException.class, true);
        retryableExceptions.put(IllegalStateException.class, true);
        
        // Configure which exceptions should not trigger retry
        retryableExceptions.put(IllegalArgumentException.class, false);
        
        return new SimpleRetryPolicy(MAX_ATTEMPTS, retryableExceptions, true);
    }
} 