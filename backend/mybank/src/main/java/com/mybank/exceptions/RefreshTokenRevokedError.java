package com.mybank.exceptions;

public class RefreshTokenRevokedError extends RuntimeException {
    public RefreshTokenRevokedError(String message) {
        super(message);
    }
}
