package com.mybank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegisterRequest(

        @NotBlank
        String name,

        @NotBlank
        String cpf,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String password,

        @NotBlank
        String phone,

        @NotNull
        LocalDate birthDate
) {
}