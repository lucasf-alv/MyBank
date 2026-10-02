package com.mybank.exceptions;

public class InvalidCredentialsError extends RuntimeException {
    public InvalidCredentialsError(String message) {
        super(message);
    }
}
