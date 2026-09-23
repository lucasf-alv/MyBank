package com.mybank.exceptions;

public class CardCancelledError extends RuntimeException {
    public CardCancelledError(String message) {
        super(message);
    }
}
