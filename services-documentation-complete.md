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
### RuleDefinitionService

**Package:** `com.aciworldwide.dealdesk.rules.service`

**Type:** Service

**Purpose:** Manages rule definitions and their metadata.

#### Methods

##### `RuleDefinition createRuleDefinition(RuleDefinition definition)`
- **Purpose:** Creates a new rule definition
- **Parameters:**
  - `definition` (`RuleDefinition`): The definition to create
- **Returns:** The created definition with ID
- **Description:** Stores metadata about rules and their parameters.

##### `List<RuleDefinition> getAllRuleDefinitions()`
- **Purpose:** Gets all available rule definitions
- **Returns:** List of all rule definitions
- **Description:** Used for presenting available rules in UIs.

##### `RuleDefinition getRuleDefinitionById(String id)`
- **Purpose:** Gets a specific rule definition
- **Parameters:**
  - `id` (`String`): The definition ID
- **Returns:** The requested definition
- **Exceptions:**
  - `RuleNotFoundException`: If definition doesn't exist
- **Description:** Retrieves detailed metadata about a rule.

### RuleExecutionService

**Package:** `com.aciworldwide.dealdesk.rules.service`

**Type:** Service

**Purpose:** Executes business rules against facts.

#### Methods

##### `Map<String, Object> executeRule(String ruleId, Map<String, Object> facts)`
- **Purpose:** Executes a specific rule against provided facts
- **Parameters:**
  - `ruleId` (`String`): The rule to execute
  - `facts` (`Map<String, Object>`): Input data
- **Returns:** Result map with outcomes
- **Exceptions:**
  - `RuleNotFoundException`: If rule doesn't exist
  - `RuleExecutionException`: If execution fails
- **Description:** Core method for rule-based decision making.

##### `List<Map<String, Object>> executeRules(List<String> ruleIds, Map<String, Object> facts)`
- **Purpose:** Executes multiple rules in sequence
- **Parameters:**
  - `ruleIds` (`List<String>`): Rules to execute
  - `facts` (`Map<String, Object>`): Input data
- **Returns:** List of result maps
- **Description:** Batch execution for efficiency.

### RuleValidationService

**Package:** `com.aciworldwide.dealdesk.rules.service`

**Type:** Service

**Purpose:** Validates rule syntax and structure.

#### Methods

##### `List<String> validateRule(DealRule rule)`
- **Purpose:** Validates a rule's structure and logic
- **Parameters:**
  - `rule` (`DealRule`): The rule to validate
- **Returns:** List of validation errors, if any
- **Description:** Ensures rules are well-formed before storage.

##### `boolean isValidRuleExpression(String expression)`
- **Purpose:** Checks if a rule expression is syntactically valid
- **Parameters:**
  - `expression` (`String`): The rule expression
- **Returns:** `true` if valid, `false` otherwise
- **Description:** Parses and validates rule expressions.

### RuleVersioningService

**Package:** `com.aciworldwide.dealdesk.rules.service`

**Type:** Service

**Purpose:** Manages rule versioning and history.

#### Methods

##### `RuleVersion createVersion(DealRule rule, String modifiedBy, String changeDescription)`
- **Purpose:** Creates a new version of a rule
- **Parameters:**
  - `rule` (`DealRule`): The updated rule
  - `modifiedBy` (`String`): User making the change
  - `changeDescription` (`String`): Description of changes
- **Returns:** The new version record
- **Description:** Tracks changes to rules over time.

##### `List<RuleVersion> getVersionHistory(String ruleId)`
- **Purpose:** Gets version history for a rule
- **Parameters:**
  - `ruleId` (`String`): The rule ID
- **Returns:** List of versions in chronological order
- **Description:** Shows complete audit trail of changes.

##### `DealRule rollbackToVersion(String ruleId, int version)`
- **Purpose:** Reverts a rule to a specific version
- **Parameters:**
  - `ruleId` (`String`): The rule ID
  - `version` (`int`): Target version
- **Returns:** The rolled-back rule
- **Exceptions:**
  - `RuleNotFoundException`: If rule or version doesn't exist
- **Description:** Creates a new version based on historical state.

##### `RuleVersion getSpecificVersion(String ruleId, int version)`
- **Purpose:** Retrieves a specific version
- **Parameters:**
  - `ruleId` (`String`): The rule ID
  - `version` (`int`): The version number
- **Returns:** The requested version
- **Exceptions:**
  - `RuleNotFoundException`: If not found
- **Description:** Used for comparison and auditing.
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
  - `IllegalArgumentException`: If expirationDate is null

---

### DealServiceImpl

**Package:** `com.aciworldwide.dealdesk.service.impl`

**Type:** Class implementing `DealService`

**Purpose:** Provides the implementation of deal management operations defined in the DealService interface.

**Dependencies:**
- `DealRepository` - For database operations on deals
- `SalesforceService` - For integration with Salesforce
- `PricingRuleEngine` - For evaluating pricing rules
- `TCVRuleExecutorService` - For executing Total Contract Value rules
- `DealValidationRuleExecutorService` - For validating deals against business rules
- `DealStatusRuleExecutorService` - For executing status-related rules

**Transaction Management:**
- Uses Spring's `@Transactional` annotations for database operations
- Read operations are marked as `readOnly=true` for optimization

**Caching:**
- Uses Spring's `@Cacheable` for frequently accessed data like `getDealById`

#### Method Implementations

##### `Deal createDeal(Deal deal)`
- Validates the deal is not null
- Calls `validateNewDeal` to check business rules
- Sets initial status to DRAFT
- Executes TCV rules
- Validates Salesforce opportunity existence
- Syncs to Salesforce
- Saves in repository

##### `Deal getDealById(String id)`
- Cached operation
- Logs fetch operation
- Returns deal or throws `DealNotFoundException`

##### `Deal updateDeal(String id, Deal deal)`
- Transactional operation
- Validates inputs
- Gets existing deal
- Validates update against business rules
- Updates fields and calculates TCV if needed
- Syncs with Salesforce
- Handles Salesforce exceptions
- Logs update status

##### `void deleteDeal(String id)`
- Checks that deal is in DRAFT status
- Throws `InvalidDealStateException` if not allowed
- Deletes from repository

##### `List<Deal> getAllDeals()`
- Simply delegates to repository's `findAll()`
##### `List<Deal> getDealsByStatus(DealStatus status)`
- Delegates to repository's filtered query

##### `List<Deal> getDealsByAccount(String accountId)`
- Delegates to repository's filtered query

##### `List<Deal> getDealsBySalesRep(String salesRepId)`
- Delegates to repository's filtered query

##### `Deal submitForApproval(String id)`
- Verifies deal is in DRAFT status
- Sets status to SUBMITTED
- Executes status rules
- Saves updated deal

##### `Deal approveDeal(String id, String approverUserId)`
- Verifies deal is in SUBMITTED status
- Updates status to APPROVED
- Records approver and timestamp
- Executes status rules
- Syncs with Salesforce
- Saves updated deal

##### `Deal rejectDeal(String id, String rejectorUserId, String reason)`
- Updates status to REJECTED
- Records reason
- Executes status rules
- Saves updated deal

##### `Deal cancelDeal(String id, String reason)`
- Updates status to CANCELLED
- Records reason
- Executes status rules
- Saves updated deal

##### `List<Deal> batchUpdateStatus(List<String> ids, DealStatus newStatus)`
- Processes deals in parallel
- Groups into batches for performance
- Handles exceptions for individual deals
- Returns list of all updated deals

##### `void batchSyncWithSalesforce(List<String> ids)`
- Retrieves all deals by IDs
- Syncs each with Salesforce

##### `Deal updateFromSalesforce(String opportunityId)`
- Finds deal by opportunity ID
- Updates from Salesforce data

##### `void syncPricing(String id)`
- Executes TCV rules
- Syncs pricing with Salesforce
- Gets quote ID if pricing model exists
- Evaluates pricing rules
- Validates price rules
- Saves updated deal

##### `Deal syncWithSalesforce(String id)`
- Gets deal by ID
- Syncs with Salesforce
- Evaluates pricing rules
- Saves updated deal

##### `boolean validateSalesforceOpportunity(String opportunityId)`
- Delegates to SalesforceService

##### `long countDealsByStatus(DealStatus status)`
- Finds deals by status and counts them

##### `BigDecimal calculateTotalValue(DealStatus status)`
- Gets deals by status
- Sums their values using Java 8 Stream API

##### `List<Deal> findExpiredDeals(ZonedDateTime expirationDate)`
- Gets all deals
- Filters by status and update date
- Returns matching deals

##### `List<Deal> getHighValueDeals(BigDecimal minValue, DealStatus status)`
- Delegates to repository's specialized query

##### `List<Deal> getRecentDeals(ZonedDateTime since, List<DealStatus> statuses)`
- Delegates to repository's specialized query

**Private Utility Methods:**

##### `private List<String> validateNewDeal(Deal deal)`
- Executes validation rules
- Throws exception if violations exist
- Returns list of validation violations

##### `private void validateDealUpdate(Deal existingDeal, Deal newDeal)`
- Executes validation rules
- Throws exception if violations exist
- Executes TCV rules if pricing changed

##### `private void updateDealFields(Deal existingDeal, Deal newDeal)`
- Checks if pricing changed
- Updates all fields
- Syncs pricing if needed

##### `private boolean isPricingChanged(Deal existingDeal, Deal newDeal)`
- Compares pricing models to detect changes

**Exception Handling:**
- Uses try-catch blocks for Salesforce integration operations
- Logs errors and wraps exceptions in application-specific exceptions
- Includes detailed error messages for troubleshooting

**Versioning and Auditing:**
- Uses Spring Data's `@Version` for optimistic locking
- Records last modified user and timestamp with `@LastModifiedBy` and `@LastModifiedDate`

**Performance Considerations:**
- Batches large operations
- Uses parallel processing for bulk updates
- Caches frequently accessed data
- Uses read-only transactions when possible
---

### SalesforceService

**Package:** `com.aciworldwide.dealdesk.service`

**Type:** Interface

**Purpose:** Defines the contract for Salesforce integration operations, handling authentication, data synchronization, and CPQ (Configure, Price, Quote) operations.

**Retry Logic:**
- Uses Spring's `@Retryable` annotation for critical operations
- Defines retry strategies with backoff for resilience

#### Method Groups

##### Authentication Operations

###### `String authenticate()`
- **Purpose:** Authenticates with Salesforce and returns a session ID
- **Returns:** The session ID for authenticated access
- **Exceptions:** 
  - `SalesforceIntegrationException`: If authentication fails
- **Retry:** 3 attempts with exponential backoff (1s, 2s, 4s)

###### `boolean validateSession(String sessionId)`
- **Purpose:** Validates if the given session ID is still valid
- **Parameters:**
  - `sessionId` (`String`): The session ID to validate
- **Returns:** `true` if session is valid, `false` otherwise

###### `void refreshToken()`
- **Purpose:** Refreshes the OAuth token for continued access
- **Exceptions:**
  - `SalesforceIntegrationException`: If token refresh fails
- **Retry:** 3 attempts with exponential backoff

##### Opportunity Operations

###### `Map<String, Object> getOpportunity(String opportunityId)`
- **Purpose:** Retrieves opportunity details from Salesforce
- **Parameters:**
  - `opportunityId` (`String`): The Salesforce opportunity ID
- **Returns:** Map of opportunity fields and values
- **Exceptions:**
  - `SalesforceIntegrationException`: If retrieval fails

###### `void updateOpportunity(String opportunityId, Map<String, Object> fields)`
- **Purpose:** Updates an opportunity in Salesforce
- **Parameters:**
  - `opportunityId` (`String`): The opportunity ID to update
  - `fields` (`Map<String, Object>`): Field names and values to update

###### `boolean validateOpportunityExists(String opportunityId)`
- **Purpose:** Verifies an opportunity exists in Salesforce
- **Parameters:**
  - `opportunityId` (`String`): The opportunity ID to check
- **Returns:** `true` if exists, `false` otherwise

###### `List<String> getOpportunityProducts(String opportunityId)`
- **Purpose:** Gets products associated with an opportunity
- **Parameters:**
  - `opportunityId` (`String`): The opportunity ID
- **Returns:** List of product IDs or codes

##### Deal Sync Operations

###### `Deal syncDealToOpportunity(Deal deal)`
- **Purpose:** Syncs deal data to Salesforce opportunity
- **Parameters:**
  - `deal` (`Deal`): The deal to sync
- **Returns:** The deal with updated fields from Salesforce
- **Exceptions:**
  - `SalesforceIntegrationException`: If sync fails
- **Retry:** 3 attempts with exponential backoff

###### `Deal handleSyncFailure(SalesforceIntegrationException e, Deal deal)`
- **Purpose:** Recovery method for sync failures
- **Parameters:**
  - `e` (`SalesforceIntegrationException`): The exception that occurred
  - `deal` (`Deal`): The deal that failed to sync
- **Returns:** The deal in its pre-sync state

###### `Deal updateDealFromOpportunity(Deal deal)`
- **Purpose:** Updates deal with data from Salesforce
- **Parameters:**
  - `deal` (`Deal`): The deal to update
- **Returns:** Updated deal

###### `void syncQuotePricing(Deal deal)`
- **Purpose:** Syncs pricing data between deal and quote
- **Parameters:**
  - `deal` (`Deal`): The deal to sync pricing for
##### CPQ Operations

###### `String createQuote(String opportunityId, Deal deal)`
- **Purpose:** Creates a quote in Salesforce CPQ
- **Parameters:**
  - `opportunityId` (`String`): The opportunity ID
  - `deal` (`Deal`): Deal data for the quote
- **Returns:** The ID of the created quote

###### `void updateQuote(String quoteId, Deal deal)`
- **Purpose:** Updates a quote in Salesforce CPQ
- **Parameters:**
  - `quoteId` (`String`): The quote ID
  - `deal` (`Deal`): Updated deal data

###### `Map<String, Object> getQuote(String quoteId)`
- **Purpose:** Gets quote details from Salesforce
- **Parameters:**
  - `quoteId` (`String`): The quote ID
- **Returns:** Map of quote fields and values

###### `List<Map<String, Object>> getQuoteLineItems(String quoteId)`
- **Purpose:** Gets line items for a quote
- **Parameters:**
  - `quoteId` (`String`): The quote ID
- **Returns:** List of line item data

###### `void updateQuoteLineItems(String quoteId, List<Map<String, Object>> lineItems)`
- **Purpose:** Updates line items for a quote
- **Parameters:**
  - `quoteId` (`String`): The quote ID
  - `lineItems` (`List<Map<String, Object>>`): Line items to update

###### `BigDecimal calculateQuoteTotalPrice(String quoteId)`
- **Purpose:** Calculates the total price of a quote
- **Parameters:**
  - `quoteId` (`String`): The quote ID
- **Returns:** The total price

###### `void validateQuoteExists(String quoteId)`
- **Purpose:** Verifies a quote exists in Salesforce
- **Parameters:**
  - `quoteId` (`String`): The quote ID to validate
- **Retry:** 3 attempts with exponential backoff

###### `void syncPriceRules(String priceRuleId, String quoteId)`
- **Purpose:** Syncs price rules to a quote
- **Parameters:**
  - `priceRuleId` (`String`): The price rule ID
  - `quoteId` (`String`): The quote ID
- **Retry:** 3 attempts with exponential backoff

##### Approval Operations

###### `void approveQuote(String quoteId, String approverId)`
- **Purpose:** Approves a quote in Salesforce
- **Parameters:**
  - `quoteId` (`String`): The quote ID
  - `approverId` (`String`): The ID of the approver

###### `void rejectQuote(String quoteId, String rejectorId, String reason)`
- **Purpose:** Rejects a quote in Salesforce
- **Parameters:**
  - `quoteId` (`String`): The quote ID
  - `rejectorId` (`String`): The ID of the rejector
  - `reason` (`String`): Reason for rejection

###### `List<Map<String, Object>> getApprovalHistory(String quoteId)`
- **Purpose:** Gets approval history for a quote
- **Parameters:**
  - `quoteId` (`String`): The quote ID
- **Returns:** List of approval history entries

###### `boolean isApprovalRequired(Deal deal)`
- **Purpose:** Checks if approval is required for a deal
- **Parameters:**
  - `deal` (`Deal`): The deal to check
- **Returns:** `true` if approval required, `false` otherwise

###### `void submitForApproval(String quoteId, Deal deal)`
- **Purpose:** Submits a quote for approval
- **Parameters:**
  - `quoteId` (`String`): The quote ID
  - `deal` (`Deal`): Associated deal data

###### `List<String> getApprovers(String quoteId)`
- **Purpose:** Gets list of approvers for a quote
- **Parameters:**
  - `quoteId` (`String`): The quote ID
- **Returns:** List of approver IDs

##### Product Operations

###### `List<Map<String, Object>> getProducts(List<String> productCodes)`
- **Purpose:** Gets product information from Salesforce
- **Parameters:**
  - `productCodes` (`List<String>`): List of product codes
- **Returns:** List of product data
###### `Map<String, BigDecimal> getProductPrices(List<String> productCodes, String pricebookId)`
- **Purpose:** Gets prices for products
- **Parameters:**
  - `productCodes` (`List<String>`): List of product codes
  - `pricebookId` (`String`): The pricebook ID to use
- **Returns:** Map of product codes to prices

###### `void validateProducts(List<String> productCodes)`
- **Purpose:** Validates products exist in Salesforce
- **Parameters:**
  - `productCodes` (`List<String>`): List of product codes to validate

##### CPQ Price Rules

###### `List<Map<String, Object>> evaluatePriceRules(String quoteId)`
- **Purpose:** Evaluates price rules for a quote
- **Parameters:**
  - `quoteId` (`String`): The quote ID
- **Returns:** List of evaluated rule results

###### `void applyPriceRules(String quoteId)`
- **Purpose:** Applies price rules to a quote
- **Parameters:**
  - `quoteId` (`String`): The quote ID

###### `void validatePriceRules(String quoteId)`
- **Purpose:** Validates price rules for a quote
- **Parameters:**
  - `quoteId` (`String`): The quote ID

##### CPQ Documents

###### `byte[] generateQuoteDocument(String quoteId, String templateId)`
- **Purpose:** Generates document for a quote
- **Parameters:**
  - `quoteId` (`String`): The quote ID
  - `templateId` (`String`): The template ID
- **Returns:** Document content as byte array

###### `List<Map<String, Object>> getAvailableTemplates(String quoteId)`
- **Purpose:** Gets available templates for a quote
- **Parameters:**
  - `quoteId` (`String`): The quote ID
- **Returns:** List of template information

###### `void attachDocument(String quoteId, String name, byte[] content, String contentType)`
- **Purpose:** Attaches a document to a quote
- **Parameters:**
  - `quoteId` (`String`): The quote ID
  - `name` (`String`): Document name
  - `content` (`byte[]`): Document content
  - `contentType` (`String`): MIME type

---

## Rule Engine Services

### RuleService

**Package:** `com.aciworldwide.dealdesk.rules.service`

**Type:** Class

**Purpose:** Provides core functionality for managing business rules, including CRUD operations, version management, and rule evaluation.

**Dependencies:**
- `RuleCachingService` - For rule caching and retrieval
- `RuleVersioningService` - For version control and rollback

**Transaction Management:**
- Uses Spring's `@Transactional` annotations for database operations

#### Methods

##### `DealRule getRuleById(String ruleId)`
- **Purpose:** Retrieves a rule by its ID
- **Parameters:**
  - `ruleId` (`String`): The rule ID to retrieve
- **Returns:** The rule if found
- **Exceptions:**
  - `RuleNotFoundException`: If rule doesn't exist
  - `IllegalArgumentException`: If ID is null or empty
- **Description:** Fetches the rule from the cache or database.

##### `void updateRule(DealRule rule, String modifiedBy, String changeDescription)`
- **Purpose:** Updates an existing rule with change tracking
- **Parameters:**
  - `rule` (`DealRule`): The rule with updated fields
  - `modifiedBy` (`String`): User ID making the change
  - `changeDescription` (`String`): Description of the changes
- **Returns:** `void`
- **Exceptions:**
  - `RuleValidationException`: If validation fails
  - `IllegalArgumentException`: If parameters are invalid
- **Transaction:** Creates a new transaction
- **Description:** Validates the update, creates a version, and persists changes.
##### `void deleteRule(String ruleId)`
- **Purpose:** Deletes a rule from the system
- **Parameters:**
  - `ruleId` (`String`): The rule ID to delete
- **Returns:** `void`
- **Exceptions:**
  - `RuleNotFoundException`: If rule doesn't exist
  - `IllegalArgumentException`: If ID is null or empty
- **Transaction:** Creates a new transaction
- **Description:** Removes the rule and invalidates the cache.

##### `List<RuleVersion> getRuleVersionHistory(String ruleId)`
- **Purpose:** Retrieves the version history for a rule
- **Parameters:**
  - `ruleId` (`String`): The rule ID
- **Returns:** List of rule versions in chronological order
- **Exceptions:**
  - `IllegalArgumentException`: If ID is null or empty
- **Description:** Provides an audit trail of all changes to the rule.

##### `DealRule rollbackRule(String ruleId, int version)`
- **Purpose:** Reverts a rule to a previous version
- **Parameters:**
  - `ruleId` (`String`): The rule ID
  - `version` (`int`): The version to roll back to
- **Returns:** The rule at the specified version
- **Exceptions:**
  - `RuleNotFoundException`: If rule doesn't exist
  - `IllegalArgumentException`: If parameters are invalid
- **Transaction:** Creates a new transaction
- **Description:** Creates a new version based on the historical version.

##### `DealRule createRule(DealRule rule)`
- **Purpose:** Creates a new rule in the system
- **Parameters:**
  - `rule` (`DealRule`): The rule to create
- **Returns:** The created rule with generated IDs
- **Exceptions:**
  - `RuleValidationException`: If validation fails
  - `IllegalArgumentException`: If rule is null
- **Description:** Validates the rule and stores it in the repository.

##### `Deal evaluateRules(Deal deal)`
- **Purpose:** Evaluates all applicable rules against a deal
- **Parameters:**
  - `deal` (`Deal`): The deal to evaluate
- **Returns:** The deal with updated fields from rule application
- **Exceptions:**
  - `IllegalArgumentException`: If deal is null
- **Description:** Applies business rules to modify or validate the deal.

### TCVRuleExecutorService

**Package:** `com.aciworldwide.dealdesk.rules.service`

**Type:** Service

**Purpose:** Executes rules specific to Total Contract Value (TCV) calculations.

**Dependencies:**
- `RulesEngine` - The rules execution engine
- `Rules` - Collection of TCV rules to execute
- `FactProvider` - Provider of facts from a deal

#### Methods

##### `void executeTCVRules(Deal deal)`
- **Purpose:** Executes TCV calculation rules for a deal
- **Parameters:**
  - `deal` (`Deal`): The deal to calculate TCV for
- **Description:** Creates facts from the deal, applies rules, and updates the deal with TCV information.

### DealStatusRuleExecutorService

**Package:** `com.aciworldwide.dealdesk.rules.service`

**Type:** Service

**Purpose:** Handles state transition rules and validations for deal status changes.

#### Methods

##### `void executeDealStatusRules(Deal deal)`
- **Purpose:** Applies status-related rules to a deal
- **Parameters:**
  - `deal` (`Deal`): The deal undergoing status change
- **Description:** Validates status changes, executes transition rules, and ensures business requirements are met.

### DealValidationRuleExecutorService

**Package:** `com.aciworldwide.dealdesk.rules.service`

**Type:** Service

**Purpose:** Executes validation rules on deals to ensure data integrity and business rule compliance.

#### Methods

##### `List<String> executeValidationRules(Deal deal)`
- **Purpose:** Validates a deal against business rules
- **Parameters:**
  - `deal` (`Deal`): The deal to validate
- **Returns:** List of validation errors, if any
- **Description:** Checks fields, relationships, and business constraints.

### RuleCachingService

**Package:** `com.aciworldwide.dealdesk.rules.service`

**Type:** Service

**Purpose:** Provides caching functionality for rules to improve performance.

#### Methods

##### `DealRule getRule(String ruleId)`
- **Purpose:** Gets a rule from cache or database
- **Parameters:**
  - `ruleId` (`String`): The rule ID
- **Returns:** The cached rule or null if not found
- **Description:** Attempts to retrieve from cache first, falls back to database.

##### `DealRule saveRule(DealRule rule)`
- **Purpose:** Saves a rule and updates cache
- **Parameters:**
  - `rule` (`DealRule`): The rule to save
- **Returns:** The saved rule
- **Description:** Persists to database and refreshes cache.

##### `void invalidateRule(String ruleId)`
- **Purpose:** Removes a rule from cache
- **Parameters:**
  - `ruleId` (`String`): The rule ID to invalidate
- **Description:** Ensures cache consistency after rule changes.

##### `void refreshCache()`
- **Purpose:** Reloads all rules into cache
- **Description:** Typically called on application startup or when cache consistency is needed.
