package com.mybank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CardTransactionCreateRequest(

        @NotNull
        @DecimalMin(value = "0.00", inclusive = false)
        BigDecimal amount,

        @NotBlank
        @Size(max = 255)
        String merchant,

        @Size(max = 255)
        String description

) {
}