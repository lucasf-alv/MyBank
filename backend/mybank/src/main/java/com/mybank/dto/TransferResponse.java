package com.mybank.dto;

import com.mybank.entities.Movements.Transfer;
import com.mybank.entities.Movements.TransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferResponse(
        UUID id,
        BigDecimal amount,
        String description,
        TransferStatus status,
        LocalDateTime createdAt,
        UUID sourceAccountId,
        UUID destinationAccountId
) {

    /*
     * Converte a entidade Transfer em um DTO
     * para retornar os dados da transferência.
     */
    public static TransferResponse from(Transfer transfer) {

        return new TransferResponse(
                transfer.getId(),
                transfer.getAmount(),
                transfer.getDescription(),
                transfer.getStatus(),
                transfer.getCreatedAt(),
                transfer.getSourceAccount().getId(),
                transfer.getDestinationAccount().getId()
        );
    }
}