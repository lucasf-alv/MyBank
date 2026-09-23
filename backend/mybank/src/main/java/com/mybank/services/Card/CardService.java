package com.mybank.services.Card;

import com.mybank.entities.Account.Account;
import com.mybank.entities.Card.Card;
import com.mybank.entities.Card.CardStatus;
import com.mybank.entities.Card.CardType;
import com.mybank.exceptions.CardAlreadyBlockedError;
import com.mybank.exceptions.CardAlreadyCancelledError;
import com.mybank.exceptions.CardBlockedError;
import com.mybank.exceptions.CardNotFoundError;
import com.mybank.exceptions.InvalidCardTypeError;
import com.mybank.repositories.Card.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    public Card create(
            Account account,
            String holderName,
            CardType type) {

        validateType(type);

        Card card = new Card();

        card.setNumber(generateCardNumber());
        card.setHolderName(holderName);
        card.setExpirationDate(
                LocalDate.now().plusYears(5)
        );
        card.setStatus(CardStatus.ACTIVE);
        card.setType(type);
        card.setCreatedAt(LocalDateTime.now());
        card.setAccount(account);

        return cardRepository.save(card);
    }

    public Card findById(UUID id) {
        return cardRepository.findById(id)
                .orElseThrow(() ->
                        new CardNotFoundError(
                                "Cartão não encontrado: " + id
                        )
                );
    }

    public List<Card> findByAccount(UUID accountId) {
        return cardRepository.findByAccountId(accountId);
    }

    public List<Card> findActiveCards(UUID accountId) {
        return cardRepository.findByAccountIdAndStatus(
                accountId,
                CardStatus.ACTIVE
        );
    }

    public void activate(Card card) {

        if (card.getStatus() == CardStatus.CANCELLED) {
            throw new CardAlreadyCancelledError(
                    "Um cartão cancelado não pode ser ativado."
            );
        }

        card.setStatus(CardStatus.ACTIVE);

        cardRepository.save(card);
    }

    public void block(Card card) {

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardAlreadyBlockedError(
                    "O cartão já está bloqueado"
            );
        }

        if (card.getStatus() == CardStatus.CANCELLED) {
            throw new CardAlreadyCancelledError(
                    "Cartão cancelado não pode ser bloqueado"
            );
        }

        card.setStatus(CardStatus.BLOCKED);

        cardRepository.save(card);
    }

    public void cancel(Card card) {

        if (card.getStatus() == CardStatus.CANCELLED) {
            throw new CardAlreadyCancelledError(
                    "O cartão já foi cancelado."
            );
        }

        card.setStatus(CardStatus.CANCELLED);

        cardRepository.save(card);
    }

    public void validateCard(Card card) {

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardBlockedError(
                    "Cartão bloqueado"
            );
        }

        if (card.getStatus() == CardStatus.CANCELLED) {
            throw new CardBlockedError(
                    "Cartão cancelado"
            );
        }

        if (card.getStatus() == CardStatus.EXPIRED ||
                card.getExpirationDate().isBefore(LocalDate.now())) {

            throw new CardBlockedError(
                    "Cartão expirado"
            );
        }
    }

    public boolean isActive(Card card) {
        return card.getStatus() == CardStatus.ACTIVE;
    }

    public boolean isCreditCard(Card card) {
        return card.getType() == CardType.CREDIT;
    }

    public boolean isDebitCard(Card card) {
        return card.getType() == CardType.DEBIT;
    }

    private void validateType(CardType type) {

        if (type == null) {
            throw new InvalidCardTypeError(
                    "O tipo de cartão não pode ser nulo"
            );
        }
    }

    private String generateCardNumber() {
        return String.valueOf(
                1000000000000000L +
                        (long) (Math.random() * 9000000000000000L)
        );
    }
}