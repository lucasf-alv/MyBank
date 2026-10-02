package com.mybank.exceptions;

public class UserAlreadyExistsError extends RuntimeException {
    public UserAlreadyExistsError(String message) {
        super(message);
    }
}
