package com.mybank.exceptions;

public class InvoiceAlreadyPaidError extends RuntimeException {
    public InvoiceAlreadyPaidError(String message) {
        super(message);
    }
}
