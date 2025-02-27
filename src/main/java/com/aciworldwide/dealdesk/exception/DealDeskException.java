package com.aciworldwide.dealdesk.exception;

import lombok.Getter;

@Getter
public class DealDeskException extends RuntimeException {
    
    private final String errorCode;
    private final transient Object details;

    public DealDeskException(String message) {
        this(message, "DEAL_DESK_ERROR", null);
    }

    public DealDeskException(String message, String errorCode) {
        this(message, errorCode, null);
    }

    public DealDeskException(String message, String errorCode, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public DealDeskException(String message, Throwable cause) {
        this(message, "DEAL_DESK_ERROR", null, cause);
    }

    public DealDeskException(String message, String errorCode, Object details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }
}