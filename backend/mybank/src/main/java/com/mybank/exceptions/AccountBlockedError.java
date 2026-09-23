package com.mybank.exceptions;

public class AccountBlockedError extends RuntimeException {
    public AccountBlockedError(String message) {
        super(message);
    }
}
