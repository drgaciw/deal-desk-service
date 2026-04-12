package com.aciworldwide.dealdesk.service.impl;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aciworldwide.dealdesk.exception.DealNotFoundException;
import com.aciworldwide.dealdesk.exception.InvalidDealStateException;
import com.aciworldwide.dealdesk.exception.SalesforceIntegrationException;
import com.aciworldwide.dealdesk.exception.SalesforceUpdateException;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.repository.DealRepository;
import com.aciworldwide.dealdesk.repository.TotalValueResult;
import com.aciworldwide.dealdesk.rules.engine.PricingRuleEngine;
import com.aciworldwide.dealdesk.rules.service.DealStatusRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.DealValidationRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.TCVRuleExecutorService;
import com.aciworldwide.dealdesk.metrics.DealMetricsService;
import com.aciworldwide.dealdesk.service.DealService;
import com.aciworldwide.dealdesk.service.SalesforceService;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {
    private final DealRepository dealRepository;
    private final SalesforceService salesforceService;
    private final PricingRuleEngine pricingRuleEngine;
    private final TCVRuleExecutorService tcvRuleExecutorService;
    private final DealValidationRuleExecutorService dealValidationRuleExecutorService;
    private final DealStatusRuleExecutorService dealStatusRuleExecutorService;
    private final DealMetricsService dealMetricsService;

    @Version
    private Long version;

    @Override
    public Deal createDeal(Deal deal) {
        log.debug("Entering createDeal: opportunityId={}", deal != null ? deal.getSalesforceOpportunityId() : null);
        if (deal == null) {
            throw new IllegalArgumentException("Deal cannot be null");
        }
        validateNewDeal(deal);
        deal.setStatus(DealStatus.DRAFT);

        Timer.Sample tcvSample = dealMetricsService.startTcvCalculationTimer();
        try {
            tcvRuleExecutorService.executeTCVRules(deal);
        } finally {
            dealMetricsService.stopTcvCalculationTimer(tcvSample);
        }

        if (!salesforceService.validateOpportunityExists(deal.getSalesforceOpportunityId())) {
            throw new IllegalArgumentException("Salesforce opportunity does not exist for deal: " + deal.getId());
        }

        if (dealRepository.existsBySalesforceOpportunityId(deal.getSalesforceOpportunityId())) {
            throw new IllegalArgumentException("Deal already exists for opportunity: " + deal.getSalesforceOpportunityId());
        }

        Timer.Sample sfSample = dealMetricsService.startSalesforceSyncTimer();
        try {
            salesforceService.syncDealToOpportunity(deal);
        } finally {
            dealMetricsService.stopSalesforceSyncTimer(sfSample);
        }

        Deal createdDeal = dealRepository.save(deal);
        dealMetricsService.recordDealCreated();
        log.info("Deal created: id={}, opportunityId={}, status={}",
                createdDeal.getId(), createdDeal.getSalesforceOpportunityId(), createdDeal.getStatus());
        log.debug("Exiting createDeal: id={}", createdDeal.getId());
        return createdDeal;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "deals", key = "#id")
    public Deal getDealById(String id) {
        log.debug("Fetching deal with id={}", id);
        return dealRepository.findById(id)
                .orElseThrow(() -> new DealNotFoundException("Deal not found with id: " + id));
    }

    @Override
    @Transactional
    public Deal updateDeal(String id, Deal deal) {
        log.debug("Entering updateDeal: id={}", id);
        if (deal == null) {
            throw new IllegalArgumentException("Updated deal cannot be null");
        }
        Deal existingDeal = getDealById(id);
        validateDealUpdate(existingDeal, deal);
        updateDealFields(existingDeal, deal);

        Timer.Sample tcvSample = dealMetricsService.startTcvCalculationTimer();
        try {
            tcvRuleExecutorService.executeTCVRules(existingDeal);
        } finally {
            dealMetricsService.stopTcvCalculationTimer(tcvSample);
        }

        Deal updatedDeal = dealRepository.save(existingDeal);
        Timer.Sample sfSample = dealMetricsService.startSalesforceSyncTimer();
        try {
            salesforceService.syncDealToOpportunity(updatedDeal);
        } catch (SalesforceIntegrationException e) {
            log.error("Failed to sync deal id={} with Salesforce", id, e);
            throw new SalesforceUpdateException("Failed to sync with Salesforce", e);
        } finally {
            dealMetricsService.stopSalesforceSyncTimer(sfSample);
        }
        log.info("Deal updated: id={}, status={}", id, updatedDeal.getStatus());
        log.debug("Exiting updateDeal: id={}", id);
        return updatedDeal;
    }

    @Override
    public void deleteDeal(String id) {
        log.debug("Entering deleteDeal: id={}", id);
        Deal deal = getDealById(id);
        if (deal.getStatus() != DealStatus.DRAFT) {
            log.warn("Delete attempted on non-DRAFT deal id={}, status={}", id, deal.getStatus());
            throw new InvalidDealStateException(deal.getStatus(), "delete");
        }
        dealRepository.delete(deal);
        log.info("Deal deleted: id={}", id);
        log.debug("Exiting deleteDeal: id={}", id);
    }

    @Override
    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    @Override
    public List<Deal> getDealsByStatus(DealStatus status) {
        return dealRepository.findByStatus(status);
    }

    @Override
    public List<Deal> getDealsByAccount(String accountId) {
        return dealRepository.findByAccountId(accountId);
    }

    @Override
    public List<Deal> getDealsBySalesRep(String salesRepId) {
        return dealRepository.findBySalesRepId(salesRepId);
    }

    @Override
    public Deal submitForApproval(String id) {
        log.debug("Entering submitForApproval: id={}", id);
        Deal deal = getDealById(id);
        if (!DealStatus.DRAFT.equals(deal.getStatus())) {
            log.warn("Submit-for-approval attempted on deal id={} in non-DRAFT status={}", id, deal.getStatus());
            throw new InvalidDealStateException("Cannot submit for approval deal in status: " + deal.getStatus());
        }
        deal.setStatus(DealStatus.SUBMITTED);
        dealStatusRuleExecutorService.executeDealStatusRules(deal);
        Deal savedDeal = dealRepository.save(deal);
        log.info("Deal submitted for approval: id={}, status={}", id, savedDeal.getStatus());
        log.debug("Exiting submitForApproval: id={}", id);
        return savedDeal;
    }

    @Override
    public Deal approveDeal(String id, String approverUserId) {
        log.debug("Entering approveDeal: id={}, approver={}", id, approverUserId);
        Deal deal = getDealById(id);
        if (!DealStatus.SUBMITTED.equals(deal.getStatus())) {
            log.warn("Approval attempted on deal id={} in non-SUBMITTED status={}", id, deal.getStatus());
            throw new InvalidDealStateException("Cannot approve deal in status: " + deal.getStatus());
        }
        deal.setStatus(DealStatus.APPROVED);
        deal.setApprovedBy(approverUserId);
        deal.setApprovedAt(ZonedDateTime.now(ZoneId.systemDefault()));
        dealStatusRuleExecutorService.executeDealStatusRules(deal);
        Timer.Sample sfSample = dealMetricsService.startSalesforceSyncTimer();
        try {
            salesforceService.syncDealToOpportunity(deal);
        } finally {
            dealMetricsService.stopSalesforceSyncTimer(sfSample);
        }
        Deal savedDeal = dealRepository.save(deal);
        dealMetricsService.recordDealApproved();
        log.info("Deal approved: id={}, approver={}, status={}", id, approverUserId, savedDeal.getStatus());
        log.debug("Exiting approveDeal: id={}", id);
        return savedDeal;
    }

    @Override
    public Deal rejectDeal(String id, String rejectorUserId, String reason) {
        log.debug("Entering rejectDeal: id={}, rejector={}", id, rejectorUserId);
        Deal deal = getDealById(id);
        deal.setStatus(DealStatus.REJECTED);
        deal.setNotes(reason);
        dealStatusRuleExecutorService.executeDealStatusRules(deal);
        Deal savedDeal = dealRepository.save(deal);
        dealMetricsService.recordDealRejected();
        log.info("Deal rejected: id={}, rejector={}, status={}", id, rejectorUserId, savedDeal.getStatus());
        log.debug("Exiting rejectDeal: id={}", id);
        return savedDeal;
    }

    @Override
    public Deal cancelDeal(String id, String reason) {
        log.debug("Entering cancelDeal: id={}", id);
        Deal deal = getDealById(id);
        deal.setStatus(DealStatus.CANCELLED);
        deal.setNotes(reason);
        dealStatusRuleExecutorService.executeDealStatusRules(deal);
        Deal savedDeal = dealRepository.save(deal);
        log.info("Deal cancelled: id={}, status={}", id, savedDeal.getStatus());
        log.debug("Exiting cancelDeal: id={}", id);
        return savedDeal;
    }

    @Override
    public List<Deal> batchUpdateStatus(List<String> ids, DealStatus newStatus) {
        List<Deal> deals = dealRepository.findAllById(ids);
        AtomicInteger counter = new AtomicInteger(0);
        int batchSize = 100;
        return deals.parallelStream()
            .peek(deal -> {
                try {
                    deal.setStatus(newStatus);
                    dealStatusRuleExecutorService.executeDealStatusRules(deal);
                } catch (Exception e) {
                    log.error("Failed to update status for deal {}: {}", deal.getId(), e.getMessage(), e);
                    throw new RuntimeException("Batch status update failed", e);
                }
            })
            .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / batchSize))
            .values()
            .stream()
            .map(dealRepository::saveAll)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    @Override
    public void batchSyncWithSalesforce(List<String> ids) {
        List<Deal> deals = dealRepository.findAllById(ids);
        salesforceService.batchUpdateOpportunities(deals);
    }

    @Override
    public Deal updateFromSalesforce(String opportunityId) {
        Deal deal = dealRepository.findBySalesforceOpportunityId(opportunityId)
                .orElseThrow(() -> new DealNotFoundException("Deal not found for opportunity id: " + opportunityId));
        return salesforceService.updateDealFromOpportunity(deal);
    }

    @Override
    public void syncPricing(String id) {
        Deal deal = getDealById(id);
        tcvRuleExecutorService.executeTCVRules(deal);
        salesforceService.syncQuotePricing(deal);
        if (deal.getPricingModel() != null) {
            String quoteId = salesforceService.getQuote(deal.getSalesforceOpportunityId())
                    .get("Id").toString();
            pricingRuleEngine.evaluateRules(deal);
            salesforceService.validatePriceRules(quoteId);
        }
        dealRepository.save(deal);
    }

    @Override
    public boolean validateSalesforceOpportunity(String opportunityId) {
        return salesforceService.validateOpportunityExists(opportunityId);
    }

    @Override
    public long countDealsByStatus(DealStatus status) {
        return dealRepository.countByStatus(status);
    }

    @Override
    public BigDecimal calculateTotalValue(DealStatus status) {
        TotalValueResult result = dealRepository.calculateTotalValueByStatus(status);
        return result != null && result.getTotal() != null ? result.getTotal() : BigDecimal.ZERO;
    }

    @Override
    public List<Deal> findExpiredDeals(ZonedDateTime expirationDate) {
        return dealRepository.findByStatusAndUpdatedAtBefore(DealStatus.SUBMITTED, expirationDate);
    }

    @Override
    public Deal syncWithSalesforce(String id) {
        log.debug("Entering syncWithSalesforce: id={}", id);
        Deal deal = getDealById(id);
        Timer.Sample sfSample = dealMetricsService.startSalesforceSyncTimer();
        Deal syncedDeal;
        try {
            syncedDeal = salesforceService.syncDealToOpportunity(deal);
        } finally {
            dealMetricsService.stopSalesforceSyncTimer(sfSample);
        }
        pricingRuleEngine.evaluateRules(syncedDeal);
        Deal savedDeal = dealRepository.save(syncedDeal);
        log.info("Deal synced with Salesforce: id={}", id);
        log.debug("Exiting syncWithSalesforce: id={}", id);
        return savedDeal;
    }

    @Override
    public List<Deal> getHighValueDeals(BigDecimal minValue, DealStatus status) {
        return dealRepository.findHighValueDeals(minValue, status);
    }

    @Override
    public List<Deal> getRecentDeals(ZonedDateTime since, List<DealStatus> statuses) {
        return dealRepository.findRecentDeals(since, statuses);
    }

    private List<String> validateNewDeal(Deal deal) {
        List<String> violations = dealValidationRuleExecutorService.executeValidationRules(deal);
        if (!violations.isEmpty()) {
            throw new InvalidDealStateException("Deal validation failed: " + String.join(", ", violations));
        }
        return violations;
    }

    private void validateDealUpdate(Deal existingDeal, Deal newDeal) {
        List<String> violations = dealValidationRuleExecutorService.executeValidationRules(newDeal);
        if (!violations.isEmpty()) {
            throw new InvalidDealStateException("Deal update validation failed: " + String.join(", ", violations));
        }
        if (isPricingChanged(existingDeal, newDeal)) {
            tcvRuleExecutorService.executeTCVRules(newDeal);
        }
    }

    private void updateDealFields(Deal existingDeal, Deal newDeal) {
        boolean pricingChanged = isPricingChanged(existingDeal, newDeal);
        existingDeal.setName(newDeal.getName());
        existingDeal.setDescription(newDeal.getDescription());
        existingDeal.setValue(newDeal.getValue());
        existingDeal.setProducts(newDeal.getProducts());
        existingDeal.setAccountId(newDeal.getAccountId());
        existingDeal.setAccountName(newDeal.getAccountName());
        existingDeal.setSalesRepId(newDeal.getSalesRepId());
        existingDeal.setSalesRepName(newDeal.getSalesRepName());
        existingDeal.setNotes(newDeal.getNotes());
        if (pricingChanged) {
            existingDeal.setPricingModel(newDeal.getPricingModel());
            syncPricing(existingDeal.getId());
        }
    }

    private boolean isPricingChanged(Deal existingDeal, Deal newDeal) {
        if (existingDeal.getPricingModel() == null && newDeal.getPricingModel() == null) {
            return false;
        }
        if (existingDeal.getPricingModel() == null || newDeal.getPricingModel() == null) {
            return true;
        }
        return !existingDeal.getPricingModel().equals(newDeal.getPricingModel());
    }

    @LastModifiedBy
    private String lastModifiedBy;

    @LastModifiedDate
    private ZonedDateTime lastModifiedDate;
}