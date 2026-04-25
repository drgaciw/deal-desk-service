package com.aciworldwide.dealdesk.exception;

import java.util.Map;

public class TcvCalculationException extends DealDeskException {

    public TcvCalculationException(String message) {
        super(message, "TCV_CALCULATION_ERROR");
    }

    public TcvCalculationException(String message, String component, Throwable cause) {
        super(message, "TCV_CALCULATION_ERROR", Map.of("component", component), cause);
    }
}
