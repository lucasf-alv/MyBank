package com.mybank.exceptions;

public class RefreshTokenExpiredError extends RuntimeException {
    public RefreshTokenExpiredError(String message) {
        super(message);
    }
}
