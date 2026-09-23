package com.mybank.exceptions;

public class TransferNotFoundError extends RuntimeException {
    public TransferNotFoundError(String message) {
        super(message);
    }
}
