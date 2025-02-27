package com.aciworldwide.dealdesk.exception;

import com.aciworldwide.dealdesk.model.DealStatus;
import java.util.Map;

public class InvalidDealStateException extends DealDeskException {
    
    public InvalidDealStateException(DealStatus currentStatus, DealStatus targetStatus) {
        super(
            String.format("Invalid deal status transition from %s to %s", currentStatus, targetStatus),
            "INVALID_DEAL_STATE",
            Map.of(
                "currentStatus", currentStatus,
                "targetStatus", targetStatus
            )
        );
    }

    public InvalidDealStateException(DealStatus currentStatus, String action) {
        super(
            String.format("Cannot perform action '%s' on deal in status %s", action, currentStatus),
            "INVALID_DEAL_STATE",
            Map.of(
                "currentStatus", currentStatus,
                "action", action
            )
        );
    }

    public InvalidDealStateException(String message) {
        super(message, "INVALID_DEAL_STATE");
    }
}