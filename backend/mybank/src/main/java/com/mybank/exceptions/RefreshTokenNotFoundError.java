package com.mybank.exceptions;

public class RefreshTokenNotFoundError extends RuntimeException {
    public RefreshTokenNotFoundError(String message) {
        super(message);
    }
}
