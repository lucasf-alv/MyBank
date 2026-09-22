package com.mybank.repositories.AdditionalFeatures;

import com.mybank.entities.AdditionalFeatures.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}