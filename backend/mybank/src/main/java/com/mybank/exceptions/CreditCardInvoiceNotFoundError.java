package com.mybank.exceptions;

public class CreditCardInvoiceNotFoundError extends RuntimeException {
    public CreditCardInvoiceNotFoundError(String message) {
        super(message);
    }
}
