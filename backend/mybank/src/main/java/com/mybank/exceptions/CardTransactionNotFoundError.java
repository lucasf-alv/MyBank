package com.mybank.exceptions;

public class CardTransactionNotFoundError extends RuntimeException {
    public CardTransactionNotFoundError(String message) {
        super(message);
    }
}
