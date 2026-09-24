package com.mybank.exceptions;

public class PixKeyNotFoundError extends RuntimeException {
    public PixKeyNotFoundError(String message) {
        super(message);
    }
}
