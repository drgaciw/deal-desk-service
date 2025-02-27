package com.aciworldwide.dealdesk.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.aciworldwide.dealdesk.model.tcv.ContingentRevenue;
import com.aciworldwide.dealdesk.model.tcv.DiscountsAndAdjustments;
import com.aciworldwide.dealdesk.model.tcv.PricingModel;
import com.aciworldwide.dealdesk.model.tcv.TCVCalculation;
import com.aciworldwide.dealdesk.model.tcv.TCVComponent;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deals")
public class Deal {
    @Id
    private String id;
    
    private String name;
    
    private String description;
    
    private BigDecimal value;
    
    private DealStatus status;
    
    private String salesforceOpportunityId;
    
    private ZonedDateTime createdAt;
    
    private ZonedDateTime statusChangedAt;
    
    private ZonedDateTime updatedAt;
    
    private String accountId;
    
    private String accountName;
    
    private String salesRepId;
    
    private String salesRepName;
    
    private String notes;
    
    private ZonedDateTime approvedAt;
    
    private String approvedBy;
    
    private ZonedDateTime startDate;
    
    private ZonedDateTime endDate;
    
    private List<String> products;
    
    private List<TCVComponent> components;
    
    private PricingModel pricingModel;
    
    private DiscountsAndAdjustments adjustments;
    
    private ContingentRevenue contingentRevenue;
    
    private TCVCalculation tcvCalculation;

    // Salesforce sync fields
    private boolean synced;
    private String syncError;
    private ZonedDateTime lastSyncAt;
    private boolean syncedWithSalesforce;
    private ZonedDateTime lastSyncedAt;
    private String lastSyncStatus;
    private String lastSyncError;

    public void setStatus(DealStatus status) {
        this.status = status;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public void setApprovedAt(ZonedDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
}