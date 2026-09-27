package com.mybank.exceptions;

public class NotificationAlreadyReadError extends RuntimeException {
    public NotificationAlreadyReadError(String message) {
        super(message);
    }
}
