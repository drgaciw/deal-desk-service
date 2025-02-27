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