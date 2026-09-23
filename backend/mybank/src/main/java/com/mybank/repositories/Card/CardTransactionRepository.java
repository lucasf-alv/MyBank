package com.mybank.repositories.Card;

import com.mybank.entities.Card.CardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CardTransactionRepository
        extends JpaRepository<CardTransaction, UUID> {

    List<CardTransaction> findByCardId(UUID cardId);

    List<CardTransaction> findByInvoiceId(UUID invoiceId);
}