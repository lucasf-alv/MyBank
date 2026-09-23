package com.mybank.exceptions;

public class CardExpiredError extends RuntimeException {
    public CardExpiredError(String message) {
        super(message);
    }
}
