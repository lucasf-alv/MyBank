package com.mybank.dto;

import com.mybank.entities.PIX.PixKey;
import com.mybank.entities.PIX.PixKeyStatus;
import com.mybank.entities.PIX.PixKeyType;

import java.time.LocalDateTime;
import java.util.UUID;

public record PixKeyResponse(
        UUID id,
        String key,
        PixKeyType type,
        PixKeyStatus status,
        LocalDateTime createdAt,
        UUID accountId
) {

    /*
     * Converte a entidade PixKey em um DTO
     * para retornar somente os dados necessários.
     */
    public static PixKeyResponse from(PixKey pixKey) {

        return new PixKeyResponse(
                pixKey.getId(),
                pixKey.getKey(),
                pixKey.getType(),
                pixKey.getStatus(),
                pixKey.getCreatedAt(),
                pixKey.getAccount().getId()
        );
    }
}