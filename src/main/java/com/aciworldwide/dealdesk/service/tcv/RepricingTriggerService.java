package com.aciworldwide.dealdesk.service.tcv;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aciworldwide.dealdesk.exception.SalesforceIntegrationException;
import com.aciworldwide.dealdesk.model.tcv.RepricingTriggers;
import com.aciworldwide.dealdesk.repository.tcv.RepricingTriggerRepository;
import com.aciworldwide.dealdesk.service.SalesforceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepricingTriggerService {
    private final RepricingTriggerRepository triggerRepository;
    private final SalesforceService salesforceService;

    @Transactional
    public RepricingTriggers syncWithCPQ(String triggerId, String quoteId) {
        log.debug("Syncing repricing trigger {} with CPQ quote {}", triggerId, quoteId);
        
        RepricingTriggers trigger = triggerRepository.findById(triggerId)
            .orElseThrow(() -> new IllegalArgumentException("Trigger not found: " + triggerId));

        try {
            // Validate and sync with Salesforce CPQ
            salesforceService.validateQuoteExists(quoteId);
            trigger.validateWithCPQ(quoteId, salesforceService);
            
            // Sync price rules to Salesforce
            salesforceService.syncPriceRules(trigger.getPriceRuleId(), quoteId);
            trigger.syncToCPQ(quoteId, salesforceService);
            
            // Update sync status
            trigger.setSyncedWithCPQ(true);
            trigger.setLastSyncTimestamp(LocalDateTime.now());
            trigger.setLastSyncError(null);
            
            return triggerRepository.save(trigger);
        } catch (SalesforceIntegrationException e) {
            log.error("Salesforce integration error while syncing trigger {} with CPQ: {}", triggerId, e.getMessage());
            updateSyncFailureStatus(trigger, e);
            return triggerRepository.save(trigger);
        } catch (Exception e) {
            log.error("Failed to sync trigger {} with CPQ: {}", triggerId, e.getMessage());
            updateSyncFailureStatus(trigger, e);
            return triggerRepository.save(trigger);
        }
    }

    private void updateSyncFailureStatus(RepricingTriggers trigger, Exception e) {
        trigger.setSyncedWithCPQ(false);
        trigger.setLastSyncTimestamp(LocalDateTime.now());
        trigger.setLastSyncError(e.getMessage());
    }

    public RepricingTriggers validateThresholds(String triggerId) {
        log.debug("Validating thresholds for trigger {}", triggerId);
        
        RepricingTriggers trigger = triggerRepository.findById(triggerId)
            .orElseThrow(() -> new IllegalArgumentException("Trigger not found: " + triggerId));

        trigger.validateThresholds();
        return trigger;
    }

    public RepricingTriggers save(RepricingTriggers trigger) {
        log.debug("Saving repricing trigger: {}", trigger.getId());
        
        trigger.validateThresholds();
        return triggerRepository.save(trigger);
    }
}