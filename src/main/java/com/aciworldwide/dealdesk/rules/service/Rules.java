package com.aciworldwide.dealdesk.rules.service;

import org.jeasy.rules.api.Rule;
import java.util.ArrayList;
import java.util.List;

// This class serves as a container for rules.
// It allows registering new rules and retrieving the registered rules.
public class Rules {

    private final List<Rule> rules = new ArrayList<>();
    
    // Registers a new rule
    public void register(Rule rule) {
        rules.add(rule);
    }
    
    // Returns the list of currently registered rules
    public List<Rule> getRules() {
        return rules;
    }
}
