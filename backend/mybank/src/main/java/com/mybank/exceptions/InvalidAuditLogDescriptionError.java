package com.mybank.exceptions;

public class InvalidAuditLogDescriptionError extends RuntimeException {
    public InvalidAuditLogDescriptionError(String message) {
        super(message);
    }
}
