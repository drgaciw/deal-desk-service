package com.aciworldwide.dealdesk.exception;

public class TCVValidationException extends RuntimeException {
    private final String errorCode;
    private final String fieldName;

    public TCVValidationException(String errorCode, String message, String fieldName) {
        super(message);
        this.errorCode = errorCode;
        this.fieldName = fieldName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getFieldName() {
        return fieldName;
    }
}