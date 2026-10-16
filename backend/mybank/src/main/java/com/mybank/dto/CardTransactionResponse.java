package com.mybank.dto;

import com.mybank.entities.Card.CardTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CardTransactionResponse(
        UUID id,
        BigDecimal amount,
        String merchant,
        String description,
        LocalDateTime createdAt,
        UUID cardId,
        UUID invoiceId
) {

    /*
     * Converte a entidade CardTransaction em um DTO.
     *
     * O invoiceId será null quando a compra não estiver
     * associada a uma fatura, como em uma compra no débito.
     */
    public static CardTransactionResponse from(
            CardTransaction transaction) {

        return new CardTransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getMerchant(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getCard().getId(),
                transaction.getInvoice() == null
                        ? null
                        : transaction.getInvoice().getId()
        );
    }
}