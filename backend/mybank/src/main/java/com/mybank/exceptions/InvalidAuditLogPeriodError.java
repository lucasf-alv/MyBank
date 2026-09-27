package com.mybank.exceptions;

public class InvalidAuditLogPeriodError extends RuntimeException {
    public InvalidAuditLogPeriodError(String message) {
        super(message);
    }
}
