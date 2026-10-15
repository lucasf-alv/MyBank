package com.mybank.dto;

import com.mybank.entities.Account.AccountType;
import jakarta.validation.constraints.NotNull;

public record AccountCreateRequest(

        @NotNull
        AccountType type

) {
}