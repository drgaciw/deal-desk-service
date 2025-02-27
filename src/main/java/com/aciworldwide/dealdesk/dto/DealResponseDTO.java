package com.aciworldwide.dealdesk.dto;

import com.aciworldwide.dealdesk.model.DealStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

@Data
public class DealResponseDTO {
    
    private String id;
    private String name;
    private String description;
    private String salesforceOpportunityId;
    private DealStatus status;
    private BigDecimal value;
    private Set<String> products;
    private String accountId;
    private String accountName;
    private String salesRepId;
    private String salesRepName;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime approvedAt;
    
    private String approvedBy;
    private String notes;
    
    // Audit fields
    private String createdBy;
    private String updatedBy;
    
    // Salesforce sync fields
    private boolean synced;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime lastSyncAt;
    private String syncError;
    
    // Additional fields for API responses
    private boolean syncedWithSalesforce;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime lastSyncedAt;
    private String lastSyncStatus;
    private String lastSyncError;
    
    // Calculated fields
    private int daysInCurrentStatus;
    private boolean requiresApproval;
    private String nextAction;
    private Set<String> availableActions;
}