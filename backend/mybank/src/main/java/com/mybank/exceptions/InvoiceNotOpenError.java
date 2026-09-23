package com.mybank.exceptions;

public class InvoiceNotOpenError extends RuntimeException {
    public InvoiceNotOpenError(String message) {
        super(message);
    }
}
