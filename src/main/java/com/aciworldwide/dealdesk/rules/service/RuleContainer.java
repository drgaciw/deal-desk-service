package com.aciworldwide.dealdesk.rules.service;

import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.Rules;
import java.util.ArrayList;
import java.util.List;

// This class serves as a container for rules and now extends org.jeasy.rules.api.Rules.
public class RuleContainer extends Rules {

    private final List<Rule> rules = new ArrayList<>();
    
    // Registers a new rule
    public void register(Rule rule) {
        if (!rules.contains(rule)) {
            rules.add(rule);
        }
    }
    
    // Unregisters a rule
    public void unregister(Rule rule) {
        rules.removeIf(r -> r.getName().equals(rule.getName()));
    }
    
    // Returns the list of currently registered rules
    public List<Rule> getRules() {
        return new ArrayList<>(rules);
    }
}
