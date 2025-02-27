package com.aciworldwide.dealdesk.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "salesforce")
@Validated
@Getter
@Setter
public class SalesforceConfig {
    
    @NotBlank
    private String baseUrl;
    
    @NotBlank
    private String clientId;
    
    @NotBlank
    private String clientSecret;
    
    @NotBlank
    private String username;
    
    @NotBlank
    private String password;
    
    @NotBlank
    private String securityToken;
    
    private String apiVersion = "v57.0";
    
    @NotNull
    private Duration connectionTimeout = Duration.ofSeconds(30);
    
    @NotNull
    private Duration readTimeout = Duration.ofSeconds(30);
    
    @NotNull
    private Duration writeTimeout = Duration.ofSeconds(30);
    
    private int maxRetries = 3;
    
    private Duration retryDelay = Duration.ofSeconds(2);
    
    private boolean sandboxMode = false;
    
    // CPQ specific configurations
    private String cpqNamespace = "SBQQ";
    
    @NotBlank
    private String opportunityPricebookId;
    
    private boolean enableQuoteSync = true;
    
    private boolean enableProductSync = true;
    
    private Duration quoteSyncInterval = Duration.ofMinutes(15);
    
    // Bulk API configurations
    private int bulkApiBatchSize = 1000;
    
    private Duration bulkApiPollInterval = Duration.ofSeconds(5);
    
    private int bulkApiMaxPollCount = 2000;
    
    // Cache configurations
    private boolean enableMetadataCache = true;
    
    private Duration metadataCacheDuration = Duration.ofHours(24);
    
    // Custom field mappings
    private String opportunityIdField = "OpportunityId__c";
    
    private String quoteIdField = "SBQQ__QuoteId__c";
    
    private String dealStatusField = "DealStatus__c";
    
    public String getApiEndpoint() {
        return baseUrl + "/services/data/" + apiVersion;
    }
    
    public String getBulkApiEndpoint() {
        return baseUrl + "/services/async/" + apiVersion;
    }
    
    public String getAuthEndpoint() {
        return baseUrl + "/services/oauth2/token";
    }
}