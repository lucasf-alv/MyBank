package com.mybank.repositories.Card;

import com.mybank.entities.Card.CreditCardInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditCardInvoiceRepository extends JpaRepository<CreditCardInvoice, UUID> {
}