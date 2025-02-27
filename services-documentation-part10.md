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