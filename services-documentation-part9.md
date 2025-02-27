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