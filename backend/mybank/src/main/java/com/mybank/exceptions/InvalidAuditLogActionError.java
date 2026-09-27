package com.mybank.exceptions;

public class InvalidAuditLogActionError extends RuntimeException {
    public InvalidAuditLogActionError(String message) {
        super(message);
    }
}
