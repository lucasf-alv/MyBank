package com.mybank.dto;

import com.mybank.entities.AdditionalFeatures.Notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String title,
        String message,
        boolean read,
        LocalDateTime createdAt
) {

    public static NotificationResponse from(Notification notification) {

        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}