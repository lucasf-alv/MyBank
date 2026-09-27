package com.mybank.exceptions;

public class InvalidNotificationMessageError extends RuntimeException {
    public InvalidNotificationMessageError(String message) {
        super(message);
    }
}
