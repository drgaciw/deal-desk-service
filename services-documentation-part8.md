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