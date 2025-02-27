package com.aciworldwide.dealdesk.util;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.aciworldwide.dealdesk.dto.DealRequestDTO;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;

/**
 * Factory class for creating test data objects.
 */
public class TestDataFactory {

    private static final BigDecimal DEFAULT_VALUE = new BigDecimal("100000.00");

    public static Deal createDeal() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        return Deal.builder()
            .name("Test Deal " + UUID.randomUUID())
            .description("Test Description")
            .salesforceOpportunityId("OPP-" + UUID.randomUUID())
            .status(DealStatus.DRAFT)
            .value(DEFAULT_VALUE)
            .products(List.of("Product1", "Product2"))
            .accountId("ACC-" + UUID.randomUUID())
            .accountName("Test Account")
            .salesRepId("REP-" + UUID.randomUUID())
            .salesRepName("Test Sales Rep")
            .createdAt(now)
            .updatedAt(now)
            .statusChangedAt(now)
            .startDate(now.plusDays(1))
            .endDate(now.plusYears(1))
            .components(new ArrayList<>())
            .synced(false)
            .syncError(null)
            .lastSyncAt(null)
            .syncedWithSalesforce(false)
            .lastSyncedAt(null)
            .lastSyncStatus(null)
            .lastSyncError(null)
            .build();
    }

    public static DealRequestDTO createDealRequest() {
        DealRequestDTO request = new DealRequestDTO();
        request.setName("Test Deal " + UUID.randomUUID());
        request.setDescription("Test Description");
        request.setSalesforceOpportunityId("OPP-" + UUID.randomUUID());
        request.setValue(new BigDecimal("100000.00"));
        request.setProducts(new HashSet<>(List.of("Product1", "Product2")));
        request.setAccountId("ACC-" + UUID.randomUUID());
        request.setAccountName("Test Account");
        request.setSalesRepId("REP-" + UUID.randomUUID());
        request.setSalesRepName("Test Sales Rep");
        request.setCurrency("USD");
        return request;
    }

    public static List<Deal> createDeals(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> createDeal())
            .collect(Collectors.toList());
    }

    public static Deal createDealWithStatus(DealStatus status) {
        Deal baseDeal = createDeal();
        return Deal.builder()
            .name(baseDeal.getName())
            .description(baseDeal.getDescription())
            .salesforceOpportunityId(baseDeal.getSalesforceOpportunityId())
            .status(status)
            .value(baseDeal.getValue())
            .products(baseDeal.getProducts())
            .accountId(baseDeal.getAccountId())
            .accountName(baseDeal.getAccountName())
            .salesRepId(baseDeal.getSalesRepId())
            .salesRepName(baseDeal.getSalesRepName())
            .createdAt(baseDeal.getCreatedAt())
            .updatedAt(baseDeal.getUpdatedAt())
            .statusChangedAt(baseDeal.getStatusChangedAt())
            .startDate(baseDeal.getStartDate())
            .endDate(baseDeal.getEndDate())
            .components(baseDeal.getComponents())
            .synced(baseDeal.isSynced())
            .syncError(baseDeal.getSyncError())
            .lastSyncAt(baseDeal.getLastSyncAt())
            .syncedWithSalesforce(baseDeal.isSyncedWithSalesforce())
            .lastSyncedAt(baseDeal.getLastSyncedAt())
            .lastSyncStatus(baseDeal.getLastSyncStatus())
            .lastSyncError(baseDeal.getLastSyncError())
            .build();
    }

    /**
     * Creates a specified number of deals with the given status.
     *
     * @param status The status to set for all created deals
     * @param count The number of deals to create
     * @return A list of deals with the specified status
     */
    public static List<Deal> createDealsWithStatus(DealStatus status, int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> createDealWithStatus(status))
            .collect(Collectors.toList());
    }

    public static Deal createHighValueDeal() {
        Deal baseDeal = createDeal();
        return Deal.builder()
            .name(baseDeal.getName())
            .description(baseDeal.getDescription())
            .salesforceOpportunityId(baseDeal.getSalesforceOpportunityId())
            .status(baseDeal.getStatus())
            .value(new BigDecimal("1000000.00"))
            .products(baseDeal.getProducts())
            .accountId(baseDeal.getAccountId())
            .accountName(baseDeal.getAccountName())
            .salesRepId(baseDeal.getSalesRepId())
            .salesRepName(baseDeal.getSalesRepName())
            .createdAt(baseDeal.getCreatedAt())
            .updatedAt(baseDeal.getUpdatedAt())
            .statusChangedAt(baseDeal.getStatusChangedAt())
            .startDate(baseDeal.getStartDate())
            .endDate(baseDeal.getEndDate())
            .components(baseDeal.getComponents())
            .synced(baseDeal.isSynced())
            .syncError(baseDeal.getSyncError())
            .lastSyncAt(baseDeal.getLastSyncAt())
            .syncedWithSalesforce(baseDeal.isSyncedWithSalesforce())
            .lastSyncedAt(baseDeal.getLastSyncedAt())
            .lastSyncStatus(baseDeal.getLastSyncStatus())
            .lastSyncError(baseDeal.getLastSyncError())
            .build();
    }

    public static Deal createDealForAccount(String accountId) {
        Deal baseDeal = createDeal();
        return Deal.builder()
            .name(baseDeal.getName())
            .description(baseDeal.getDescription())
            .salesforceOpportunityId(baseDeal.getSalesforceOpportunityId())
            .status(baseDeal.getStatus())
            .value(baseDeal.getValue())
            .products(baseDeal.getProducts())
            .accountId(accountId)
            .accountName("Account for " + accountId)
            .salesRepId(baseDeal.getSalesRepId())
            .salesRepName(baseDeal.getSalesRepName())
            .createdAt(baseDeal.getCreatedAt())
            .updatedAt(baseDeal.getUpdatedAt())
            .statusChangedAt(baseDeal.getStatusChangedAt())
            .startDate(baseDeal.getStartDate())
            .endDate(baseDeal.getEndDate())
            .components(baseDeal.getComponents())
            .synced(baseDeal.isSynced())
            .syncError(baseDeal.getSyncError())
            .lastSyncAt(baseDeal.getLastSyncAt())
            .syncedWithSalesforce(baseDeal.isSyncedWithSalesforce())
            .lastSyncedAt(baseDeal.getLastSyncedAt())
            .lastSyncStatus(baseDeal.getLastSyncStatus())
            .lastSyncError(baseDeal.getLastSyncError())
            .build();
    }

    public static Deal createDealForSalesRep(String salesRepId) {
        Deal baseDeal = createDeal();
        return Deal.builder()
            .name(baseDeal.getName())
            .description(baseDeal.getDescription())
            .salesforceOpportunityId(baseDeal.getSalesforceOpportunityId())
            .status(baseDeal.getStatus())
            .value(baseDeal.getValue())
            .products(baseDeal.getProducts())
            .accountId(baseDeal.getAccountId())
            .accountName(baseDeal.getAccountName())
            .salesRepId(salesRepId)
            .salesRepName("Sales Rep " + salesRepId)
            .createdAt(baseDeal.getCreatedAt())
            .updatedAt(baseDeal.getUpdatedAt())
            .statusChangedAt(baseDeal.getStatusChangedAt())
            .startDate(baseDeal.getStartDate())
            .endDate(baseDeal.getEndDate())
            .components(baseDeal.getComponents())
            .synced(baseDeal.isSynced())
            .syncError(baseDeal.getSyncError())
            .lastSyncAt(baseDeal.getLastSyncAt())
            .syncedWithSalesforce(baseDeal.isSyncedWithSalesforce())
            .lastSyncedAt(baseDeal.getLastSyncedAt())
            .lastSyncStatus(baseDeal.getLastSyncStatus())
            .lastSyncError(baseDeal.getLastSyncError())
            .build();
    }

    /**
     * Creates a deal without a Salesforce opportunity ID for testing validation.
     */
    public static Deal createDealWithoutOpportunityId() {
        Deal baseDeal = createDeal();
        return Deal.builder()
            .name(baseDeal.getName())
            .description(baseDeal.getDescription())
            .salesforceOpportunityId(null)
            .status(baseDeal.getStatus())
            .value(baseDeal.getValue())
            .products(baseDeal.getProducts())
            .accountId(baseDeal.getAccountId())
            .accountName(baseDeal.getAccountName())
            .salesRepId(baseDeal.getSalesRepId())
            .salesRepName(baseDeal.getSalesRepName())
            .createdAt(baseDeal.getCreatedAt())
            .updatedAt(baseDeal.getUpdatedAt())
            .statusChangedAt(baseDeal.getStatusChangedAt())
            .startDate(baseDeal.getStartDate())
            .endDate(baseDeal.getEndDate())
            .components(baseDeal.getComponents())
            .build();
    }

    /**
     * Creates a deal with zero TCV value for testing validation.
     */
    public static Deal createDealWithZeroTCV() {
        Deal baseDeal = createDeal();
        return Deal.builder()
            .name(baseDeal.getName())
            .description(baseDeal.getDescription())
            .salesforceOpportunityId(baseDeal.getSalesforceOpportunityId())
            .status(baseDeal.getStatus())
            .value(BigDecimal.ZERO)
            .products(baseDeal.getProducts())
            .accountId(baseDeal.getAccountId())
            .accountName(baseDeal.getAccountName())
            .salesRepId(baseDeal.getSalesRepId())
            .salesRepName(baseDeal.getSalesRepName())
            .createdAt(baseDeal.getCreatedAt())
            .updatedAt(baseDeal.getUpdatedAt())
            .statusChangedAt(baseDeal.getStatusChangedAt())
            .startDate(baseDeal.getStartDate())
            .endDate(baseDeal.getEndDate())
            .components(baseDeal.getComponents())
            .build();
    }

    /**
     * Creates a deal with negative TCV value for testing validation.
     */
    public static Deal createDealWithNegativeTCV() {
        Deal baseDeal = createDeal();
        return Deal.builder()
            .name(baseDeal.getName())
            .description(baseDeal.getDescription())
            .salesforceOpportunityId(baseDeal.getSalesforceOpportunityId())
            .status(baseDeal.getStatus())
            .value(new BigDecimal("-1000.00"))
            .products(baseDeal.getProducts())
            .accountId(baseDeal.getAccountId())
            .accountName(baseDeal.getAccountName())
            .salesRepId(baseDeal.getSalesRepId())
            .salesRepName(baseDeal.getSalesRepName())
            .createdAt(baseDeal.getCreatedAt())
            .updatedAt(baseDeal.getUpdatedAt())
            .statusChangedAt(baseDeal.getStatusChangedAt())
            .startDate(baseDeal.getStartDate())
            .endDate(baseDeal.getEndDate())
            .components(baseDeal.getComponents())
            .build();
    }
}