package com.mybank.repositories.AdditionalFeatures;

import com.mybank.entities.AdditionalFeatures.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByUserId(UUID userId);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );
}