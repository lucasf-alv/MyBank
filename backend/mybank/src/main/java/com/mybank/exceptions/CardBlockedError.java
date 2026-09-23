package com.mybank.exceptions;

public class CardBlockedError extends RuntimeException {
    public CardBlockedError(String message) {
        super(message);
    }
}
