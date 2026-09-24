package com.mybank.exceptions;

public class InvoiceNotOverdueError extends RuntimeException {
    public InvoiceNotOverdueError(String message) {
        super(message);
    }
}
