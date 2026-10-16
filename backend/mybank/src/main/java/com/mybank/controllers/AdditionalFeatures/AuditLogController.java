package com.mybank.controllers.AdditionalFeatures;

import com.mybank.dto.AuditLogResponse;
import com.mybank.entities.AdditionalFeatures.AuditLog;
import com.mybank.services.AdditionalFeatures.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@Tag(
        name = "Audit Logs",
        description = "Endpoints for consulting user audit logs"
)
public class AuditLogController {

    private final AuditLogService auditLogService;

    /*
     * Lista o histórico de auditoria do usuário autenticado.
     */
    @GetMapping
    @Operation(
            summary = "List audit logs",
            description = "Lists audit logs belonging to the authenticated user."
    )
    public ResponseEntity<List<AuditLogResponse>> findMyAuditLogs(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = getAuthenticatedUserId(jwt);

        List<AuditLogResponse> auditLogs =
                auditLogService.findByUserId(userId)
                        .stream()
                        .map(AuditLogResponse::from)
                        .toList();

        return ResponseEntity.ok(auditLogs);
    }

    /*
     * Consulta um registro específico do histórico.
     *
     * O serviço verifica se o registro pertence
     * ao usuário autenticado.
     */
    @GetMapping("/{auditLogId}")
    @Operation(
            summary = "Find audit log by ID",
            description = "Finds an audit log belonging to the authenticated user."
    )
    public ResponseEntity<AuditLogResponse> findById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID auditLogId) {

        UUID userId = getAuthenticatedUserId(jwt);

        AuditLog auditLog =
                auditLogService.findByIdAndUserId(
                        auditLogId,
                        userId
                );

        return ResponseEntity.ok(
                AuditLogResponse.from(auditLog)
        );
    }

    /*
     * Extrai o UUID do usuário a partir do subject do JWT.
     */
    private UUID getAuthenticatedUserId(Jwt jwt) {

        return UUID.fromString(
                jwt.getSubject()
        );
    }
}