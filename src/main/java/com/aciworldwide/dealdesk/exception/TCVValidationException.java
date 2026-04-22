package com.aciworldwide.dealdesk.exception;

import java.util.Map;

public class TCVValidationException extends DealDeskException {

    public TCVValidationException(String errorCode, String message, String fieldName) {
        super(message, errorCode, Map.of("fieldName", fieldName));
    }

    public String getFieldName() {
        Object details = getDetails();
        if (details instanceof Map<?, ?> m) {
            return (String) m.get("fieldName");
        }
        return null;
    }
}