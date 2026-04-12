package com.aciworldwide.dealdesk.dto;

import com.aciworldwide.dealdesk.model.DealStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

@Data
@Schema(description = "Deal data returned by the API")
public class DealResponseDTO {
    
    @Schema(description = "Unique deal identifier")
    private String id;

    @Schema(description = "Name of the deal")
    private String name;

    @Schema(description = "Brief description of the deal")
    private String description;

    @Schema(description = "Salesforce Opportunity ID linked to this deal")
    private String salesforceOpportunityId;

    @Schema(description = "Current status of the deal")
    private DealStatus status;

    @Schema(description = "Total deal value")
    private BigDecimal value;

    @Schema(description = "Product identifiers included in this deal")
    private Set<String> products;

    @Schema(description = "Salesforce Account ID of the customer")
    private String accountId;

    @Schema(description = "Name of the customer account")
    private String accountName;

    @Schema(description = "ID of the sales representative owning this deal")
    private String salesRepId;

    @Schema(description = "Full name of the sales representative")
    private String salesRepName;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Schema(description = "Timestamp when the deal was created")
    private ZonedDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Schema(description = "Timestamp when the deal was last updated")
    private ZonedDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Schema(description = "Timestamp when the deal was approved")
    private ZonedDateTime approvedAt;
    
    @Schema(description = "User ID of the approver")
    private String approvedBy;

    @Schema(description = "Optional free-text notes")
    private String notes;
    
    // Audit fields
    @Schema(description = "User who created the deal")
    private String createdBy;

    @Schema(description = "User who last updated the deal")
    private String updatedBy;
    
    // Salesforce sync fields
    @Schema(description = "Whether the deal has been synced with Salesforce")
    private boolean synced;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Schema(description = "Timestamp of the last Salesforce sync attempt")
    private ZonedDateTime lastSyncAt;

    @Schema(description = "Error message from the last failed sync, if any")
    private String syncError;
    
    // Additional fields for API responses
    @Schema(description = "Whether the deal is successfully synced with Salesforce")
    private boolean syncedWithSalesforce;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Schema(description = "Timestamp of the last successful Salesforce sync")
    private ZonedDateTime lastSyncedAt;

    @Schema(description = "Status of the last sync operation")
    private String lastSyncStatus;

    @Schema(description = "Error message from the last sync operation, if any")
    private String lastSyncError;
    
    // Calculated fields
    @Schema(description = "Number of days the deal has been in its current status")
    private int daysInCurrentStatus;

    @Schema(description = "Whether the deal requires approval based on business rules")
    private boolean requiresApproval;

    @Schema(description = "Recommended next action for this deal")
    private String nextAction;

    @Schema(description = "Set of actions currently available for this deal")
    private Set<String> availableActions;
}