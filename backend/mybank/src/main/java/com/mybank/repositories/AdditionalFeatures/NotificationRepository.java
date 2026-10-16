package com.mybank.repositories.AdditionalFeatures;

import com.mybank.entities.AdditionalFeatures.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserId(UUID userId);
    List<Notification> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUser_IdAndReadFalseOrderByCreatedAtDesc(
            UUID userId
    );

    Optional<Notification> findByIdAndUser_Id(
            UUID notificationId,
            UUID userId
    );
}