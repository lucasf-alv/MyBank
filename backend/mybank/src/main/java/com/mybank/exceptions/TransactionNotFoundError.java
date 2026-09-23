package com.mybank.exceptions;

public class TransactionNotFoundError extends RuntimeException {
    public TransactionNotFoundError(String message) {
        super(message);
    }
}
