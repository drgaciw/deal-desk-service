package com.aciworldwide.dealdesk.exception;

import java.util.Map;

public class DuplicateDealException extends DealDeskException {

    public DuplicateDealException(String salesforceOpportunityId) {
        super(
            "Deal already exists for opportunity: " + salesforceOpportunityId,
            "DUPLICATE_DEAL",
            Map.of("salesforceOpportunityId", salesforceOpportunityId)
        );
    }
}
