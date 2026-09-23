package com.mybank.exceptions;

public class AccountNotFoundError extends RuntimeException {
    public AccountNotFoundError(String message) {
        super(message);
    }
}
