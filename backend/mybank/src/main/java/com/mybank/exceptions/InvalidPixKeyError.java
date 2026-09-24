package com.mybank.exceptions;

public class InvalidPixKeyError extends RuntimeException {
    public InvalidPixKeyError(String message) {
        super(message);
    }
}
