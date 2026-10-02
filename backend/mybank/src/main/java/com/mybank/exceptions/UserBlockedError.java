package com.mybank.exceptions;

public class UserBlockedError extends RuntimeException {
    public UserBlockedError(String message) {
        super(message);
    }
}
