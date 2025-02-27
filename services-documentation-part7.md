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