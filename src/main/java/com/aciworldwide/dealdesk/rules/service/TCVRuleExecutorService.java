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
public class TCVRuleExecutorService {
    
    private final RulesEngine rulesEngine;
    private final Rules tCVRules;
    private final FactProvider dealFactProvider;

    /**
     * Execute TCV calculation rules for a deal
     *
     * @param deal The deal to calculate TCV for
     */
    public void executeTCVRules(Deal deal) {
        log.debug("Executing TCV rules for deal: {}", deal.getId());
        
        Facts facts = new Facts();
        dealFactProvider.provideFacts(deal, facts);
        
        rulesEngine.fire(tCVRules, facts);
        
        log.debug("Completed TCV rule execution for deal: {}", deal.getId());
    }
}