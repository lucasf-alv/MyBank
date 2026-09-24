package com.mybank.exceptions;

public class InvalidPixTransferAmountError extends RuntimeException {
    public InvalidPixTransferAmountError(String message) {
        super(message);
    }
}
