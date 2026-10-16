package com.mybank.dto;

import com.mybank.entities.Card.CreditCardInvoice;
import com.mybank.entities.Card.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreditCardInvoiceResponse(
        UUID id,
        LocalDate dueDate,
        LocalDate closingDate,
        BigDecimal totalAmount,
        InvoiceStatus status,
        LocalDateTime createdAt,
        UUID cardId
) {

    public static CreditCardInvoiceResponse from(
            CreditCardInvoice invoice) {

        return new CreditCardInvoiceResponse(
                invoice.getId(),
                invoice.getDueDate(),
                invoice.getClosingDate(),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                invoice.getCreatedAt(),
                invoice.getCard().getId()
        );
    }
}