package com.mybank.exceptions;

public class InvalidAmountError extends RuntimeException {
    public InvalidAmountError(String message) {
        super(message);
    }
}
