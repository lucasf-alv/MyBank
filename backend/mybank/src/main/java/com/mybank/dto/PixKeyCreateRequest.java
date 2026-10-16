package com.mybank.dto;

import com.mybank.entities.PIX.PixKeyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PixKeyCreateRequest(

        @NotNull
        UUID accountId,

        @NotBlank
        @Size(max = 255)
        String key,

        @NotNull
        PixKeyType type

) {
}