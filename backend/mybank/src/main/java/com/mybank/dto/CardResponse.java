package com.mybank.dto;

import com.mybank.entities.Card.Card;
import com.mybank.entities.Card.CardStatus;
import com.mybank.entities.Card.CardType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CardResponse(
        UUID id,
        String number,
        String holderName,
        LocalDate expirationDate,
        CardStatus status,
        CardType type,
        LocalDateTime createdAt,
        UUID accountId
) {

    /*
     * Converte a entidade Card em um DTO.
     *
     * O número é mascarado para evitar a exposição
     * desnecessária dos dados do cartão.
     */
    public static CardResponse from(Card card) {

        return new CardResponse(
                card.getId(),
                maskNumber(card.getNumber()),
                card.getHolderName(),
                card.getExpirationDate(),
                card.getStatus(),
                card.getType(),
                card.getCreatedAt(),
                card.getAccount().getId()
        );
    }

    /*
     * Mantém somente os quatro últimos dígitos
     * visíveis na resposta da API.
     */
    private static String maskNumber(String number) {

        if (number == null || number.length() <= 4) {
            return number;
        }

        return "*".repeat(number.length() - 4)
                + number.substring(number.length() - 4);
    }
}