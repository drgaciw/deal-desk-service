package com.aciworldwide.dealdesk.rules.impl;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.service.SalesforceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealValidationRules {

    @Autowired
    private final SalesforceService salesforceService;

    @Rule(name = "validate-required-fields", description = "Validates required deal fields", priority = 1)
    public static class RequiredFieldsRule {
        
        @Condition
        public boolean evaluate(@Fact("deal") Deal deal) {
            // Always evaluate this rule
            return true;
        }

        @Action
        public void execute(@Fact("deal") Deal deal) {
            if (deal.getName() == null || deal.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Deal name is required");
            }
            
            if (deal.getSalesforceOpportunityId() == null || 
                deal.getSalesforceOpportunityId().trim().isEmpty()) {
                throw new IllegalArgumentException("Salesforce opportunity ID is required");
            }
            
            if (deal.getAccountId() == null || deal.getAccountId().trim().isEmpty()) {
                throw new IllegalArgumentException("Account ID is required");
            }
            
            log.info("Required fields validated for deal: {}", deal.getId());
        }
    }

    @Rule(name = "validate-dates", description = "Validates deal dates", priority = 2)
    public static class DateValidationRule {
        
        @Condition
        public boolean evaluate(@Fact("deal") Deal deal) {
            return deal.getStartDate() != null || deal.getEndDate() != null;
        }

        @Action
        public void execute(@Fact("deal") Deal deal) {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            
            if (deal.getStartDate() != null && deal.getStartDate().isBefore(now)) {
                throw new IllegalArgumentException("Start date cannot be in the past");
            }
            
            if (deal.getStartDate() != null && deal.getEndDate() != null && 
                deal.getEndDate().isBefore(deal.getStartDate())) {
                throw new IllegalArgumentException("End date must be after start date");
            }
            
            log.info("Date fields validated for deal: {}", deal.getId());
        }
    }

    @Rule(name = "validate-value", description = "Validates deal value", priority = 2)
    public static class ValueValidationRule {
        
        @Condition
        public boolean evaluate(@Fact("deal") Deal deal) {
            return deal.getValue() != null;
        }

        @Action
        public void execute(@Fact("deal") Deal deal) {
            if (deal.getValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Deal value must be greater than zero");
            }
            
            log.info("Value validated for deal: {}", deal.getId());
        }
    }

    @Rule(name = "validate-components", description = "Validates deal components", priority = 2)
    public static class ComponentValidationRule {
        
        @Condition
        public boolean evaluate(@Fact("deal") Deal deal) {
            return deal.getComponents() != null && !deal.getComponents().isEmpty();
        }

        @Action
        public void execute(@Fact("deal") Deal deal) {
            deal.getComponents().forEach(component -> {
                if (component.getQuantity() == null || 
                    component.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(
                        "Component quantity must be greater than zero: " + component.getName());
                }
                
                if (component.getUnitPrice() == null || 
                    component.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException(
                        "Component unit price cannot be negative: " + component.getName());
                }
            });
            
            log.info("Components validated for deal: {}", deal.getId());
        }
    }

    @Rule(name = "validate-salesforce", description = "Validates Salesforce integration", priority = 3)
    public class SalesforceValidationRule {
        
        @Condition
        public boolean evaluate(@Fact("deal") Deal deal) {
            return deal.getSalesforceOpportunityId() != null;
        }

        @Action
        public void execute(@Fact("deal") Deal deal) {
            if (!salesforceService.validateOpportunityExists(deal.getSalesforceOpportunityId())) {
                throw new IllegalArgumentException(
                    "Invalid Salesforce opportunity ID: " + deal.getSalesforceOpportunityId());
            }
            
            // Validate products exist in Salesforce
            if (deal.getProducts() != null && !deal.getProducts().isEmpty()) {
                salesforceService.validateProducts(List.copyOf(deal.getProducts()));
            }
            
            log.info("Salesforce integration validated for deal: {}", deal.getId());
        }
    }
}