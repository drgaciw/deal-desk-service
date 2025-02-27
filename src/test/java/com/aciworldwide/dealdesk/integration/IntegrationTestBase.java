package com.aciworldwide.dealdesk.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class IntegrationTestBase {

    public static MongoDBContainer mongoDBContainer = 
        new MongoDBContainer(DockerImageName.parse("mongo:6.0.9"));

    static {
        // Start the container once before any tests run.
        mongoDBContainer.start();
    }

    // Bind the container's replica set URI and Salesforce properties to the Spring context.
    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        
        // Provide dummy Salesforce configuration properties to satisfy binding validation.
        registry.add("salesforce.clientId", () -> "dummyClientId");
        registry.add("salesforce.opportunityPricebookId", () -> "dummyOpportunityPricebookId");
        registry.add("salesforce.password", () -> "dummyPassword");
        registry.add("salesforce.username", () -> "dummyUsername");
        registry.add("salesforce.clientSecret", () -> "dummyClientSecret");
        registry.add("salesforce.baseUrl", () -> "https://dummy.salesforce.com");
        registry.add("salesforce.securityToken", () -> "dummySecurityToken");
    }
} 