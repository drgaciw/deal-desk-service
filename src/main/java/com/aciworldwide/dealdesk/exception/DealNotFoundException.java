package com.aciworldwide.dealdesk.exception;

public class DealNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DealNotFoundException(String id) {
        super("Deal not found with id: " + id);
    }

    public DealNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}