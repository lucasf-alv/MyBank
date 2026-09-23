package com.mybank.exceptions;

public class InvalidTransferAmountError extends RuntimeException {
    public InvalidTransferAmountError(String message) {
        super(message);
    }
}
