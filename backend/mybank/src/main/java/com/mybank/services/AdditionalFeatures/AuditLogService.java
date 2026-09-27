package com.mybank.services.AdditionalFeatures;

import com.mybank.entities.AdditionalFeatures.AuditLog;
import com.mybank.entities.Authentication.User;
import com.mybank.exceptions.AuditLogNotFoundError;
import com.mybank.exceptions.InvalidAuditLogActionError;
import com.mybank.exceptions.InvalidAuditLogDescriptionError;
import com.mybank.repositories.AdditionalFeatures.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /*
     * Cria um novo registro de auditoria.
     *
     * O registro armazena:
     * - usuário responsável pela ação;
     * - ação realizada;
     * - descrição da ação;
     * - data e hora da operação.
     */
    public AuditLog create(
            User user,
            String action,
            String description) {

        validateAction(action);
        validateDescription(description);

        AuditLog auditLog = new AuditLog();

        auditLog.setAction(action);
        auditLog.setDescription(description);
        auditLog.setCreatedAt(LocalDateTime.now());
        auditLog.setUser(user);

        return auditLogRepository.save(auditLog);
    }

    /*
     * Busca um registro de auditoria pelo ID.
     *
     * Caso o registro não exista, lança uma exceção
     * para ser tratada pelo GlobalExceptionHandler.
     */
    public AuditLog findById(UUID id) {

        return auditLogRepository.findById(id)
                .orElseThrow(() ->
                        new AuditLogNotFoundError(
                                "Audit log not found: " + id
                        )
                );
    }

    /*
     * Busca todos os registros de auditoria
     * pertencentes a um determinado usuário.
     */
    public List<AuditLog> findByUser(UUID userId) {

        return auditLogRepository
                .findByUserId(userId);
    }

    /*
     * Busca registros de auditoria de acordo
     * com a ação realizada.
     *
     * Exemplos:
     * - LOGIN
     * - LOGOUT
     * - PIX_TRANSFER
     * - ACCOUNT_CREATED
     * - CARD_BLOCKED
     */
    public List<AuditLog> findByAction(
            String action) {

        validateAction(action);

        return auditLogRepository
                .findByAction(action);
    }

    /*
     * Busca registros de auditoria realizados
     * dentro de um determinado período.
     *
     * O intervalo considera as datas inicial e final.
     */
    public List<AuditLog> findByPeriod(
            LocalDateTime start,
            LocalDateTime end) {

        if (start == null || end == null) {
            throw new InvalidAuditLogDescriptionError(
                    "Start and end dates cannot be null"
            );
        }

        if (start.isAfter(end)) {
            throw new InvalidAuditLogDescriptionError(
                    "Start date cannot be after end date"
            );
        }

        return auditLogRepository
                .findByCreatedAtBetween(start, end);
    }

    /*
     * Valida a ação registrada no log.
     *
     * A ação não pode ser nula ou vazia.
     */
    private void validateAction(String action) {

        if (action == null || action.isBlank()) {
            throw new InvalidAuditLogActionError(
                    "Audit log action cannot be null or empty"
            );
        }
    }

    /*
     * Valida a descrição do log.
     *
     * A descrição não pode ser nula ou vazia.
     */
    private void validateDescription(
            String description) {

        if (description == null ||
                description.isBlank()) {

            throw new InvalidAuditLogDescriptionError(
                    "Audit log description cannot be null or empty"
            );
        }
    }
}