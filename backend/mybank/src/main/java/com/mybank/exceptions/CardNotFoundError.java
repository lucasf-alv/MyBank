package com.mybank.exceptions;

public class CardNotFoundError extends RuntimeException {
    public CardNotFoundError(String message) {
        super(message);
    }
}
