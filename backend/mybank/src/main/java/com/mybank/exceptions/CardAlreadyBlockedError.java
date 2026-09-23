package com.mybank.exceptions;

public class CardAlreadyBlockedError extends RuntimeException {
    public CardAlreadyBlockedError(String message) {
        super(message);
    }
}
