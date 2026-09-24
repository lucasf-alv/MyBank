package com.mybank.exceptions;

public class InvoiceNotReadyToCloseError extends RuntimeException {
    public InvoiceNotReadyToCloseError(String message) {
        super(message);
    }
}
