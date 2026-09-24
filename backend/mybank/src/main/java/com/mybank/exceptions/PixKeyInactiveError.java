package com.mybank.exceptions;

public class PixKeyInactiveError extends RuntimeException {
    public PixKeyInactiveError(String message) {
        super(message);
    }
}
