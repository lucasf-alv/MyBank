package com.mybank.exceptions;

public class InvalidTransactionTypeError extends RuntimeException {
    public InvalidTransactionTypeError(String message) {
        super(message);
    }
}
