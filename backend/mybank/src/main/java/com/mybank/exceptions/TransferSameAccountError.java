package com.mybank.exceptions;

public class TransferSameAccountError extends RuntimeException {
    public TransferSameAccountError(String message) {
        super(message);
    }
}
