package com.aciworldwide.dealdesk.service.impl;

import com.aciworldwide.dealdesk.exception.SalesforceIntegrationException;

public class DealUpdateException extends RuntimeException {

    public DealUpdateException(String message, SalesforceIntegrationException cause) {
        super(message, cause);
    }

}
