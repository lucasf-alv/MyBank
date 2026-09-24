package com.mybank.exceptions;

public class PixKeyAlreadyActiveError extends RuntimeException {
    public PixKeyAlreadyActiveError(String message) {
        super(message);
    }
}
