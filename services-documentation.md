# Deal Desk Service - Comprehensive Service Documentation

## Table of Contents
- [Introduction](#introduction)
- [Core Services](#core-services)
  - [DealService](#dealservice)
  - [DealServiceImpl](#dealserviceimpl)
  - [SalesforceService](#salesforceservice)
  - [RepricingTriggerService](#repricingtriggerservice)
  - [TCVCalculationStrategy](#tcvcalculationstrategy)
- [Rule Engine Services](#rule-engine-services)
  - [RuleService](#ruleservice)
  - [TCVRuleExecutorService](#tcvruleexecutorservice)
  - [DealStatusRuleExecutorService](#dealstatusruleexecutorservice)
  - [DealValidationRuleExecutorService](#dealvalidationruleexecutorservice)
  - [RuleCachingService](#rulecachingservice)
  - [RuleDefinitionService](#ruledefinitionservice)
  - [RuleExecutionService](#ruleexecutionservice)
  - [RuleValidationService](#rulevalidationservice)
  - [RuleVersioningService](#ruleversioningservice)

## Introduction

This document provides a comprehensive reference for all service classes in the Deal Desk application. Each service is documented with its purpose, methods, parameters, return types, exceptions, dependencies, and usage notes. This documentation serves as a reference for developers working with the codebase.

---

## Core Services

### DealService

**Package:** `com.aciworldwide.dealdesk.service`

**Type:** Interface

**Purpose:** Defines the contract for deal management operations, including creation, retrieval, updates, and lifecycle management.

#### Methods

##### `Deal createDeal(Deal deal)`
- **Purpose:** Creates a new deal in the system
- **Parameters:** 
  - `deal` (`Deal`): The deal object containing all required fields
- **Returns:** The created deal with generated fields (ID, timestamps)
- **Exceptions:**
  - `IllegalArgumentException`: If required fields are missing or invalid
##### `List<Deal> getDealsByStatus(DealStatus status)`
- **Purpose:** Retrieves deals filtered by their status
- **Parameters:** 
  - `status` (`DealStatus`): The deal status to filter by (must not be null)
- **Returns:** List of deals matching the specified status
- **Exceptions:**
  - `IllegalArgumentException`: If status is null

##### `List<Deal> getDealsByAccount(String accountId)`
- **Purpose:** Retrieves deals associated with a specific account
- **Parameters:**
  - `SalesforceIntegrationException`: If there is an error communicating with Salesforce
- **Description:** Retrieves data from Salesforce and updates the local deal record.

##### `boolean validateSalesforceOpportunity(String opportunityId)`
- **Purpose:** Validates that a Salesforce opportunity exists and meets system requirements
- **Parameters:**
  - `opportunityId` (`String`): The Salesforce opportunity ID to validate (must not be null or empty)
- **Returns:** `true` if the opportunity is valid, `false` otherwise
- **Exceptions:**
  - `IllegalArgumentException`: If the opportunity ID is invalid
  - `SalesforceIntegrationException`: If there is an error communicating with Salesforce

##### `void syncPricing(String id)`
- **Purpose:** Synchronizes pricing information between the deal and Salesforce
- **Parameters:**
  - `id` (`String`): The ID of the deal to synchronize pricing for (must not be null or empty)
- **Returns:** `void`
- **Exceptions:**
  - `DealNotFoundException`: If the deal is not found
  - `IllegalArgumentException`: If the ID is invalid
  - `SalesforceIntegrationException`: If there is an error communicating with Salesforce
- **Description:** Executes TCV rules, syncs quote pricing with Salesforce, and validates price rules.

##### `List<Deal> batchUpdateStatus(List<String> ids, DealStatus newStatus)`
- **Purpose:** Updates the status of multiple deals in a single batch operation
- **Parameters:**
  - `ids` (`List<String>`): List of deal IDs to update (must not be null or empty)
  - `newStatus` (`DealStatus`): The new status to apply to all deals (must not be null)
- **Returns:** List of updated deals
- **Exceptions:**
  - `IllegalArgumentException`: If any parameter is invalid
  - `BatchOperationException`: If the operation fails for any deals
- **Description:** Updates status in parallel with batching to optimize database operations.

##### `void batchSyncWithSalesforce(List<String> ids)`
- **Purpose:** Synchronizes multiple deals with Salesforce in a single batch operation
- **Parameters:**
  - `ids` (`List<String>`): List of deal IDs to synchronize (must not be null or empty)
- **Returns:** `void`
- **Exceptions:**
  - `IllegalArgumentException`: If any parameter is invalid
  - `BatchOperationException`: If the operation fails for any deals
  - `SalesforceIntegrationException`: If there is an error communicating with Salesforce

##### `long countDealsByStatus(DealStatus status)`
- **Purpose:** Counts the number of deals with a specific status
- **Parameters:**
  - `status` (`DealStatus`): The deal status to count (must not be null)
- **Returns:** The count of deals with the specified status
- **Exceptions:**
  - `IllegalArgumentException`: If status is null

##### `BigDecimal calculateTotalValue(DealStatus status)`
- **Purpose:** Calculates the total value of all deals with a specific status
- **Parameters:**
  - `status` (`DealStatus`): The deal status to calculate total value for (must not be null)
- **Returns:** The total value as a BigDecimal
- **Exceptions:**
  - `IllegalArgumentException`: If status is null

##### `List<Deal> findExpiredDeals(ZonedDateTime expirationDate)`
- **Purpose:** Finds deals that have expired before the specified date
- **Parameters:**
  - `expirationDate` (`ZonedDateTime`): The cutoff date for expiration (must not be null)
- **Returns:** List of expired deals
- **Exceptions:**
  - `IllegalArgumentException`: If expirationDate is null
