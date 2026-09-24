package com.mybank.exceptions;

public class PixKeyAlreadyExistsError extends RuntimeException {
    public PixKeyAlreadyExistsError(String message) {
        super(message);
    }
}
