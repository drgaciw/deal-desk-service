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