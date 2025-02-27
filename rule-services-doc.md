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
