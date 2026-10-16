package com.mybank.dto;

import com.mybank.entities.PIX.PixTransfer;
import com.mybank.entities.PIX.PixTransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PixTransferResponse(
        UUID id,
        BigDecimal amount,
        String description,
        PixTransferStatus status,
        LocalDateTime createdAt,
        UUID sourceAccountId,
        UUID destinationAccountId,
        UUID pixKeyId
) {

    /*
     * Converte a entidade PixTransfer em um DTO,
     * retornando os dados necessários da transferência.
     */
    public static PixTransferResponse from(
            PixTransfer transfer) {

        return new PixTransferResponse(
                transfer.getId(),
                transfer.getAmount(),
                transfer.getDescription(),
                transfer.getStatus(),
                transfer.getCreatedAt(),
                transfer.getSourceAccount().getId(),
                transfer.getDestinationAccount().getId(),
                transfer.getPixKey().getId()
        );
    }
}