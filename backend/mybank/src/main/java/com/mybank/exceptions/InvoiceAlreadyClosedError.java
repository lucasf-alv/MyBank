package com.mybank.exceptions;

public class InvoiceAlreadyClosedError extends RuntimeException {
    public InvoiceAlreadyClosedError(String message) {
        super(message);
    }
}
