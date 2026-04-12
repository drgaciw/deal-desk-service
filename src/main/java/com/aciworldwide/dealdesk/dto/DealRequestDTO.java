package com.aciworldwide.dealdesk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Schema(description = "Request payload for creating or updating a deal")
public class DealRequestDTO {
    
    @NotBlank(message = "Deal name is required")
    @Size(min = 3, max = 100, message = "Deal name must be between 3 and 100 characters")
    @Schema(description = "Name of the deal", example = "Enterprise License 2025", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    
    @NotBlank(message = "Deal description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Brief description of the deal", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;
    
    @NotBlank(message = "Salesforce Opportunity ID is required")
    @Schema(description = "Salesforce Opportunity ID linked to this deal", example = "0063000000AbcXYZ", requiredMode = Schema.RequiredMode.REQUIRED)
    private String salesforceOpportunityId;
    
    @NotNull(message = "Deal value is required")
    @Positive(message = "Deal value must be positive")
    @Schema(description = "Total deal value in the specified currency", example = "250000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal value;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    @Schema(description = "ISO 4217 currency code", example = "USD", requiredMode = Schema.RequiredMode.REQUIRED)
    private String currency;
    
    @Schema(description = "Set of product identifiers included in this deal")
    private Set<String> products;
    
    @NotBlank(message = "Account ID is required")
    @Schema(description = "Salesforce Account ID of the customer", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountId;
    
    @NotBlank(message = "Account name is required")
    @Schema(description = "Name of the customer account", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountName;
    
    @NotBlank(message = "Sales representative ID is required")
    @Schema(description = "ID of the sales representative owning this deal", requiredMode = Schema.RequiredMode.REQUIRED)
    private String salesRepId;
    
    @NotBlank(message = "Sales representative name is required")
    @Schema(description = "Full name of the sales representative", requiredMode = Schema.RequiredMode.REQUIRED)
    private String salesRepName;
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Schema(description = "Optional free-text notes")
    private String notes;
}