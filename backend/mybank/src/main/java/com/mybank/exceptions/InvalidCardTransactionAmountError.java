package com.mybank.exceptions;

public class InvalidCardTransactionAmountError extends RuntimeException {
    public InvalidCardTransactionAmountError(String message) {
        super(message);
    }
}
