package com.mybank.repositories.Movements;

import com.mybank.entities.Movements.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByAccountId(UUID accountId);

    List<Transaction> findByAccountIdAndCreatedAtBetween(
            UUID accountId,
            LocalDateTime start,
            LocalDateTime end
    );
}