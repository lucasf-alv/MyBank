package com.mybank.repositories.Card;

import com.mybank.entities.Card.CreditCardInvoice;
import com.mybank.entities.Card.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardInvoiceRepository
        extends JpaRepository<CreditCardInvoice, UUID> {

    List<CreditCardInvoice> findByCardId(UUID cardId);

    Optional<CreditCardInvoice> findByCardIdAndStatus(
            UUID cardId,
            InvoiceStatus status
    );
}