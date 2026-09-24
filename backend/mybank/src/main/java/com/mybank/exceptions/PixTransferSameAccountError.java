package com.mybank.exceptions;

public class PixTransferSameAccountError extends RuntimeException {
    public PixTransferSameAccountError(String message) {
        super(message);
    }
}
