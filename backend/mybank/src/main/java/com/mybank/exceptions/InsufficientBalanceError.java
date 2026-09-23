package com.mybank.exceptions;

public class InsufficientBalanceError extends RuntimeException {
    public InsufficientBalanceError(String message) {
        super(message);
    }
}
