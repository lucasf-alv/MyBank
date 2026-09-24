package com.mybank.exceptions;

public class PixKeyAlreadyInactiveError extends RuntimeException {
    public PixKeyAlreadyInactiveError(String message) {
        super(message);
    }
}
