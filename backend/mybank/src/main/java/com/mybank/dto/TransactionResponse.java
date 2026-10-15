package com.mybank.dto;

import com.mybank.entities.Movements.Transaction;
import com.mybank.entities.Movements.TransactionStatus;
import com.mybank.entities.Movements.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        String description,
        BigDecimal balanceAfter,
        TransactionStatus status,
        LocalDateTime createdAt,
        UUID accountId
) {

    /*
     * Converte a entidade Transaction em um DTO
     * para retornar somente os dados necessários.
     */
    public static TransactionResponse from(
            Transaction transaction) {

        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getBalanceAfter(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                transaction.getAccount().getId()
        );
    }
}