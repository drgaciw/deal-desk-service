package com.aciworldwide.dealdesk.rules.service;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.rules.fact.FactProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealStatusRuleExecutorService {
    
    private final RulesEngine rulesEngine;
    private final Rules dealStatusRules;
    private final FactProvider dealFactProvider;

    /**
     * Execute deal status rules for a deal
     *
     * @param deal The deal to evaluate status rules for
     */
    public void executeDealStatusRules(Deal deal) {
        log.debug("Executing deal status rules for deal: {}", deal.getId());
        
        Facts facts = new Facts();
        dealFactProvider.provideFacts(deal, facts);
        
        rulesEngine.fire(dealStatusRules, facts);
        
        log.debug("Completed deal status rule execution for deal: {}", deal.getId());
    }
}