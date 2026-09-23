package com.mybank.exceptions;

public class InvoiceNotClosedError extends RuntimeException {
    public InvoiceNotClosedError(String message) {
        super(message);
    }
}
