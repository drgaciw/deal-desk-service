package com.aciworldwide.dealdesk.exception;

import com.aciworldwide.dealdesk.model.DealStatus;

public class InvalidDealStateTransitionException extends InvalidDealStateException {

    public InvalidDealStateTransitionException(DealStatus from, DealStatus to) {
        super(from, to);
    }

    public static InvalidDealStateTransitionException forSubmission(DealStatus currentStatus) {
        return new InvalidDealStateTransitionException(currentStatus, DealStatus.SUBMITTED);
    }
}
