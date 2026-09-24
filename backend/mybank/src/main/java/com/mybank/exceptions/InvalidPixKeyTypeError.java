package com.mybank.exceptions;

public class InvalidPixKeyTypeError extends RuntimeException {
    public InvalidPixKeyTypeError(String message) {
        super(message);
    }
}
