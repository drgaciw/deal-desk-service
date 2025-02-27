package com.aciworldwide.dealdesk.rules.service;

import java.util.ArrayList;
import java.util.List;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.springframework.stereotype.Service;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.rules.fact.FactProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealValidationRuleExecutorService {
    
    private final RulesEngine rulesEngine;
    private final Rules dealValidationRules;
    private final FactProvider dealFactProvider;

    /**
     * Execute validation rules for a deal.
     *
     * @param deal The deal to validate.
     * @return A list of validation messages (empty if no issues).
     */
    public List<String> executeValidationRules(Deal deal) {
        log.debug("Executing validation rules for deal: {}", deal.getId());
        
        Facts facts = new Facts();
        dealFactProvider.provideFacts(deal, facts);
        
        rulesEngine.fire(dealValidationRules, facts);
        
        log.debug("Completed validation rule execution for deal: {}", deal.getId());
        
        // Return a new empty list of validation messages.
        return new ArrayList<>();
    }
}