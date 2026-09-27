package com.mybank.exceptions;

public class AuditLogNotFoundError extends RuntimeException {
    public AuditLogNotFoundError(String message) {
        super(message);
    }
}
