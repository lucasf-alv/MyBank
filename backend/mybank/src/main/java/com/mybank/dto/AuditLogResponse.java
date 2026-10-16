package com.mybank.dto;

import com.mybank.entities.AdditionalFeatures.AuditLog;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String action,
        String description,
        String ipAddress,
        LocalDateTime createdAt
) {

    public static AuditLogResponse from(AuditLog auditLog) {

        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getAction(),
                auditLog.getDescription(),
                auditLog.getIpAddress(),
                auditLog.getCreatedAt()
        );
    }
}