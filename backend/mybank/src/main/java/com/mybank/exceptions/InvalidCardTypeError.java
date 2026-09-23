package com.mybank.exceptions;

public class InvalidCardTypeError extends RuntimeException {
    public InvalidCardTypeError(String message) {
        super(message);
    }
}
