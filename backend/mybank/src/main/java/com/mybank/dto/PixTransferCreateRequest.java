package com.mybank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record PixTransferCreateRequest(

        @NotNull
        UUID sourceAccountId,

        @NotBlank
        @Size(max = 255)
        String pixKey,

        @NotNull
        @DecimalMin(value = "0.00", inclusive = false)
        BigDecimal amount,

        @Size(max = 255)
        String description

) {
}