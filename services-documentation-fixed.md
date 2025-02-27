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
- **Description:** Performs validation of the deal, sets its initial status to DRAFT, and stores it in the repository.

##### `Deal getDealById(String id)`
- **Purpose:** Retrieves a deal by its ID
- **Parameters:**
  - `id` (`String`): The deal ID (must not be null or empty)
- **Returns:** The deal with all associated data
- **Exceptions:**
  - `DealNotFoundException`: If the deal is not found
  - `IllegalArgumentException`: If the ID is invalid
- **Cache:** Typically cached to improve performance

##### `Deal updateDeal(String id, Deal deal)`
- **Purpose:** Updates an existing deal with new data
- **Parameters:**
  - `id` (`String`): The ID of the deal to update (must not be null or empty)
  - `deal` (`Deal`): The deal object containing updated fields
- **Returns:** The updated deal
- **Exceptions:**
  - `DealNotFoundException`: If the deal is not found
  - `IllegalArgumentException`: If the ID is invalid or required fields are missing
- **Description:** Validates the update, applies changes, and synchronizes with external systems.

##### `void deleteDeal(String id)`
- **Purpose:** Deletes a deal from the system
- **Parameters:**
  - `id` (`String`): The ID of the deal to delete (must not be null or empty)
- **Returns:** `void`
- **Exceptions:**
  - `DealNotFoundException`: If the deal is not found
  - `IllegalArgumentException`: If the ID is invalid
  - `IllegalStateException`: If the deal cannot be deleted due to its current state
- **Description:** Checks if deletion is allowed based on deal state and removes it from the repository.

##### `List<Deal> getAllDeals()`
- **Purpose:** Retrieves all deals in the system
- **Parameters:** None
- **Returns:** List of all deals, ordered by creation date descending
- **Exceptions:** 
  - `DataAccessException`: If there is an issue accessing the data store

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
  - `accountId` (`String`): The account ID to filter by (must not be null or empty)
- **Returns:** List of deals associated with the account
- **Exceptions:**
  - `IllegalArgumentException`: If accountId is invalid

##### `List<Deal> getDealsBySalesRep(String salesRepId)`
- **Purpose:** Retrieves deals managed by a specific sales representative
- **Parameters:**
  - `salesRepId` (`String`): The sales representative ID to filter by (must not be null or empty)
- **Returns:** List of deals managed by the sales representative
- **Exceptions:**
  - `IllegalArgumentException`: If salesRepId is invalid

##### `List<Deal> getHighValueDeals(BigDecimal minValue, DealStatus status)`
- **Purpose:** Retrieves high-value deals that meet the minimum value threshold and status
- **Parameters:**
  - `minValue` (`BigDecimal`): The minimum deal value threshold (must be positive)
  - `status` (`DealStatus`): The deal status to filter by (must not be null)
- **Returns:** List of high-value deals meeting the criteria
- **Exceptions:**
  - `IllegalArgumentException`: If minValue is not positive or status is null

##### `List<Deal> getRecentDeals(ZonedDateTime since, List<DealStatus> statuses)`
- **Purpose:** Retrieves deals created since a specific date, filtered by status
- **Parameters:**
  - `since` (`ZonedDateTime`): The cutoff date for deal creation (must not be null)
  - `statuses` (`List<DealStatus>`): List of statuses to include (null or empty list returns all statuses)
- **Returns:** List of recent deals meeting the criteria
- **Exceptions:**
  - `IllegalArgumentException`: If since is null

##### `Deal submitForApproval(String id)`
- **Purpose:** Submits a deal for approval, transitioning it to the SUBMITTED state
- **Parameters:**
  - `id` (`String`): The ID of the deal to submit (must not be null or empty)
- **Returns:** The updated deal with new status
- **Exceptions:**
  - `DealNotFoundException`: If the deal is not found
  - `IllegalArgumentException`: If the ID is invalid
  - `IllegalStateException`: If the deal cannot be submitted in its current state
- **Description:** Validates state transition, updates status, and applies any status-related rules.

##### `Deal approveDeal(String id, String approverUserId)`
- **Purpose:** Approves a deal, transitioning it to the APPROVED state
- **Parameters:**
  - `id` (`String`): The ID of the deal to approve (must not be null or empty)
  - `approverUserId` (`String`): The ID of the user approving the deal (must not be null or empty)
- **Returns:** The approved deal
- **Exceptions:**
  - `DealNotFoundException`: If the deal is not found
  - `IllegalArgumentException`: If any parameter is invalid
  - `IllegalStateException`: If the deal cannot be approved in its current state
- **Description:** Records approver details, updates status, and synchronizes with Salesforce.

##### `Deal rejectDeal(String id, String rejectorUserId, String reason)`
- **Purpose:** Rejects a deal, transitioning it to the REJECTED state
- **Parameters:**
  - `id` (`String`): The ID of the deal to reject (must not be null or empty)
  - `rejectorUserId` (`String`): The ID of the user rejecting the deal (must not be null or empty)
  - `reason` (`String`): The reason for rejection (must not be null or empty)
- **Returns:** The rejected deal
- **Exceptions:**
  - `DealNotFoundException`: If the deal is not found
  - `IllegalArgumentException`: If any parameter is invalid
  - `IllegalStateException`: If the deal cannot be rejected in its current state
- **Description:** Records rejection reason, updates status, and applies status rules.

##### `Deal cancelDeal(String id, String reason)`
- **Purpose:** Cancels a deal, transitioning it to the CANCELLED state
- **Parameters:**
  - `id` (`String`): The ID of the deal to cancel (must not be null or empty)
  - `reason` (`String`): The reason for cancellation (must not be null or empty)
- **Returns:** The cancelled deal
- **Exceptions:**
  - `DealNotFoundException`: If the deal is not found
  - `IllegalArgumentException`: If any parameter is invalid
  - `IllegalStateException`: If the deal cannot be cancelled in its current state
- **Description:** Records cancellation reason, updates status, and applies status rules.

##### `Deal syncWithSalesforce(String id)`
- **Purpose:** Synchronizes deal data with Salesforce, updating both systems with the latest information
- **Parameters:**
  - `id` (`String`): The ID of the deal to synchronize (must not be null or empty)
- **Returns:** The updated deal with synchronized data
- **Exceptions:**
  - `DealNotFoundException`: If the deal is not found
  - `IllegalArgumentException`: If the ID is invalid
  - `SalesforceIntegrationException`: If there is an error communicating with Salesforce
- **Description:** Syncs deal data to Salesforce, evaluates pricing rules, and saves any updates.

##### `Deal updateFromSalesforce(String opportunityId)`
- **Purpose:** Updates deal data from Salesforce using the opportunity ID
- **Parameters:**
  - `opportunityId` (`String`): The Salesforce opportunity ID (must not be null or empty)
- **Returns:** The updated deal with data from Salesforce
- **Exceptions:**
  - `DealNotFoundException`: If no matching deal is found
  - `IllegalArgumentException`: If the opportunity ID is invalid
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
  - `IllegalArgumentException`: If expirationDate is null- **Returns:** `void`
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