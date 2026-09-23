package com.mybank.exceptions;

public class TransferFailedError extends RuntimeException {
    public TransferFailedError(String message) {
        super(message);
    }
}
