package com.mybank.exceptions;

public class PixTransferNotFoundError extends RuntimeException {
    public PixTransferNotFoundError(String message) {
        super(message);
    }
}
