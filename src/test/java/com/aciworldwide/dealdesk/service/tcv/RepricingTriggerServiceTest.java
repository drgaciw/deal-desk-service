package com.aciworldwide.dealdesk.service.tcv;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aciworldwide.dealdesk.model.tcv.RepricingTriggers;
import com.aciworldwide.dealdesk.repository.tcv.RepricingTriggerRepository;
import com.aciworldwide.dealdesk.service.SalesforceService;

@ExtendWith(MockitoExtension.class)
class RepricingTriggerServiceTest {

    @Mock
    private RepricingTriggerRepository triggerRepository;

    @Mock
    private SalesforceService salesforceService;

    @InjectMocks
    private RepricingTriggerService triggerService;

    @Test
    void syncWithCPQ_ShouldCallEvaluateAndApplyPriceRules() {
        // Arrange
        String triggerId = "trigger-1";
        String quoteId = "quote-1";
        RepricingTriggers trigger = RepricingTriggers.builder()
                .id(triggerId)
                .priceRuleId("rule-1")
                .build();

        when(triggerRepository.findById(triggerId)).thenReturn(Optional.of(trigger));
        when(triggerRepository.save(any(RepricingTriggers.class))).thenReturn(trigger);

        // Act
        triggerService.syncWithCPQ(triggerId, quoteId);

        // Assert
        // Verify validateQuoteExists is called (part of existing logic)
        verify(salesforceService).validateQuoteExists(quoteId);

        // Verify evaluatePriceRules is called (via validateWithCPQ)
        verify(salesforceService).evaluatePriceRules(quoteId);

        // Verify syncPriceRules is called (part of existing logic)
        verify(salesforceService).syncPriceRules("rule-1", quoteId);

        // Verify applyPriceRules is called (via syncToCPQ)
        verify(salesforceService).applyPriceRules(quoteId);

        // Verify save is called
        verify(triggerRepository).save(trigger);
    }
}
