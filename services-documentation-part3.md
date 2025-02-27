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