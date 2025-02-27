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