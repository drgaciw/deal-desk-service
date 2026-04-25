package com.aciworldwide.dealdesk.exception;

public class SalesforceUpdateException extends DealDeskException {

    public SalesforceUpdateException(String message, Throwable cause) {
        super(message, "SALESFORCE_UPDATE_ERROR", null, cause);
    }
}
