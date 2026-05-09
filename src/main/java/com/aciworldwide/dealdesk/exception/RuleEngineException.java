package com.aciworldwide.dealdesk.exception;

import java.util.Map;

public class RuleEngineException extends DealDeskException {

    public RuleEngineException(String message) {
        super(message, "RULE_ENGINE_ERROR");
    }

    public RuleEngineException(String message, String ruleId, Throwable cause) {
        super(message, "RULE_ENGINE_ERROR", Map.of("ruleId", ruleId), cause);
    }
}
