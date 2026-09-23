package com.mybank.exceptions;

public class CardAlreadyCancelledError extends RuntimeException {
    public CardAlreadyCancelledError(String message) {
        super(message);
    }
}
