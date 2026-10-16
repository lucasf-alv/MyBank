package com.mybank.dto;

import com.mybank.entities.Card.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CardCreateRequest(

        @NotNull
        UUID accountId,

        @NotBlank
        @Size(max = 255)
        String holderName,

        @NotNull
        CardType type

) {
}