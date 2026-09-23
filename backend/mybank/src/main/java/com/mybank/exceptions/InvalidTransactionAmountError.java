package com.mybank.exceptions;

public class InvalidTransactionAmountError extends RuntimeException {
    public InvalidTransactionAmountError(String message) {
        super(message);
    }
}
