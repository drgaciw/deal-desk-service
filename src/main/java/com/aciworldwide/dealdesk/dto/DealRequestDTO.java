package com.aciworldwide.dealdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class DealRequestDTO {
    
    @NotBlank(message = "Deal name is required")
    @Size(min = 3, max = 100, message = "Deal name must be between 3 and 100 characters")
    private String name;
    
    @NotBlank(message = "Deal description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @NotBlank(message = "Salesforce Opportunity ID is required")
    private String salesforceOpportunityId;
    
    @NotNull(message = "Deal value is required")
    @Positive(message = "Deal value must be positive")
    private BigDecimal value;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String currency;
    
    private Set<String> products;
    
    @NotBlank(message = "Account ID is required")
    private String accountId;
    
    @NotBlank(message = "Account name is required")
    private String accountName;
    
    @NotBlank(message = "Sales representative ID is required")
    private String salesRepId;
    
    @NotBlank(message = "Sales representative name is required")
    private String salesRepName;
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}