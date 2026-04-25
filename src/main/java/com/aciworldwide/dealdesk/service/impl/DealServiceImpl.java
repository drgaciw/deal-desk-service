package com.aciworldwide.dealdesk.service.impl;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.aciworldwide.dealdesk.service.DealService;
import com.aciworldwide.dealdesk.service.SalesforceService;

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

    @Version
    private Long version;

    @Override
    @CachePut(value = "deals", key = "#result.id")
    public Deal createDeal(Deal deal) {
        if (deal == null) {
            throw new IllegalArgumentException("Deal cannot be null");
        }
        validateNewDeal(deal);
        deal.setStatus(DealStatus.DRAFT);

        tcvRuleExecutorService.executeTCVRules(deal);

        if (!salesforceService.validateOpportunityExists(deal.getSalesforceOpportunityId())) {
            throw new IllegalArgumentException("Salesforce opportunity does not exist for deal: " + deal.getId());
        }

        if (dealRepository.existsBySalesforceOpportunityId(deal.getSalesforceOpportunityId())) {
            throw new IllegalArgumentException("Deal already exists for opportunity: " + deal.getSalesforceOpportunityId());
        }

        salesforceService.syncDealToOpportunity(deal);
        return dealRepository.save(deal);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "deals", key = "#id")
    public Deal getDealById(String id) {
        log.debug("Fetching deal with ID: {}", id);
        return dealRepository.findById(id)
                .orElseThrow(() -> new DealNotFoundException("Deal not found with id: " + id));
    }

    @Override
    @Transactional
    @CachePut(value = "deals", key = "#id")
    public Deal updateDeal(String id, Deal deal) {
        log.debug("Updating deal with ID: {}", id);
        if (deal == null) {
            throw new IllegalArgumentException("Updated deal cannot be null");
        }
        Deal existingDeal = getDealById(id);
        validateDealUpdate(existingDeal, deal);
        updateDealFields(existingDeal, deal);

        tcvRuleExecutorService.executeTCVRules(existingDeal);
        Deal updatedDeal = dealRepository.save(existingDeal);
        try {
            salesforceService.syncDealToOpportunity(updatedDeal);
        } catch (SalesforceIntegrationException e) {
            log.error("Failed to sync deal {} with Salesforce", id, e);
            throw new SalesforceUpdateException("Failed to sync with Salesforce", e);
        }
        log.info("Deal {} successfully updated", id);
        return updatedDeal;
    }

    @Override
    @CacheEvict(value = "deals", key = "#id")
    public void deleteDeal(String id) {
        Deal deal = getDealById(id);
        if (deal.getStatus() != DealStatus.DRAFT) {
            throw new InvalidDealStateException(deal.getStatus(), "delete");
        }
        dealRepository.delete(deal);
    }

    @Override
    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    @Override
    public Page<Deal> getAllDeals(Pageable pageable) {
        return dealRepository.findAll(pageable);
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
    @CachePut(value = "deals", key = "#id")
    public Deal submitForApproval(String id) {
        Deal deal = getDealById(id);
        if (!DealStatus.DRAFT.equals(deal.getStatus())) {
            throw new InvalidDealStateException("Cannot submit for approval deal in status: " + deal.getStatus());
        }
        deal.setStatus(DealStatus.SUBMITTED);
        dealStatusRuleExecutorService.executeDealStatusRules(deal);
        return dealRepository.save(deal);
    }

    @Override
    @CachePut(value = "deals", key = "#id")
    public Deal approveDeal(String id, String approverUserId) {
        Deal deal = getDealById(id);
        if (!DealStatus.SUBMITTED.equals(deal.getStatus())) {
            throw new InvalidDealStateException("Cannot approve deal in status: " + deal.getStatus());
        }
        deal.setStatus(DealStatus.APPROVED);
        deal.setApprovedBy(approverUserId);
        deal.setApprovedAt(ZonedDateTime.now(ZoneId.systemDefault()));
        dealStatusRuleExecutorService.executeDealStatusRules(deal);
        salesforceService.syncDealToOpportunity(deal);
        return dealRepository.save(deal);
    }

    @Override
    @CachePut(value = "deals", key = "#id")
    public Deal rejectDeal(String id, String rejectorUserId, String reason) {
        Deal deal = getDealById(id);
        deal.setStatus(DealStatus.REJECTED);
        deal.setNotes(reason);
        dealStatusRuleExecutorService.executeDealStatusRules(deal);
        return dealRepository.save(deal);
    }

    @Override
    @CachePut(value = "deals", key = "#id")
    public Deal cancelDeal(String id, String reason) {
        Deal deal = getDealById(id);
        deal.setStatus(DealStatus.CANCELLED);
        deal.setNotes(reason);
        dealStatusRuleExecutorService.executeDealStatusRules(deal);
        return dealRepository.save(deal);
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
    @CacheEvict(value = "deals", key = "#id")
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
    @CachePut(value = "deals", key = "#id")
    public Deal syncWithSalesforce(String id) {
        Deal deal = getDealById(id);
        Deal syncedDeal = salesforceService.syncDealToOpportunity(deal);
        pricingRuleEngine.evaluateRules(syncedDeal);
        return dealRepository.save(syncedDeal);
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