package com.mybank.exceptions;

public class NotificationNotFoundError extends RuntimeException {
    public NotificationNotFoundError(String message) {
        super(message);
    }
}
