package com.mybank.dto;

import com.mybank.entities.Account.Account;
import com.mybank.entities.Account.AccountStatus;
import com.mybank.entities.Account.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber,
        String branch,
        BigDecimal balance,
        AccountType type,
        AccountStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AccountResponse from(Account account) {

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBranch(),
                account.getBalance(),
                account.getType(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}