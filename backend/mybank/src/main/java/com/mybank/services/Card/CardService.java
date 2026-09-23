package com.mybank.services.Card;

import com.mybank.entities.Account.Account;
import com.mybank.entities.Card.Card;
import com.mybank.entities.Card.CardStatus;
import com.mybank.entities.Card.CardType;
import com.mybank.exceptions.CardAlreadyBlockedError;
import com.mybank.exceptions.CardAlreadyCancelledError;
import com.mybank.exceptions.CardBlockedError;
import com.mybank.exceptions.CardCancelledError;
import com.mybank.exceptions.CardExpiredError;
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

    /*
     * Cria um novo cartão associado a uma conta.
     *
     * O cartão começa como ACTIVE e recebe uma data de
     * expiração de cinco anos a partir da data de criação.
     */
    public Card create(
            Account account,
            String holderName,
            CardType type) {

        // Valida se o tipo do cartão foi informado.
        validateType(type);

        Card card = new Card();

        // Gera o número do cartão.
        card.setNumber(generateCardNumber());

        // Define o nome do titular do cartão.
        card.setHolderName(holderName);

        // Define a data de expiração para cinco anos a partir de hoje.
        card.setExpirationDate(
                LocalDate.now().plusYears(5)
        );

        // Novos cartões começam ativos.
        card.setStatus(CardStatus.ACTIVE);

        // Define se o cartão é de crédito ou débito.
        card.setType(type);

        // Registra a data e hora de criação do cartão.
        card.setCreatedAt(LocalDateTime.now());

        // Associa o cartão à conta.
        card.setAccount(account);

        return cardRepository.save(card);
    }

    /*
     * Busca um cartão pelo seu ID.
     *
     * Caso o cartão não seja encontrado, uma exceção
     * CardNotFoundError é lançada.
     */
    public Card findById(UUID id) {

        return cardRepository.findById(id)
                .orElseThrow(() ->
                        new CardNotFoundError(
                                "Card not found: " + id
                        )
                );
    }

    /*
     * Retorna todos os cartões associados a uma conta.
     */
    public List<Card> findByAccount(UUID accountId) {

        return cardRepository.findByAccountId(accountId);
    }

    /*
     * Retorna somente os cartões ativos de uma conta.
     */
    public List<Card> findActiveCards(UUID accountId) {

        return cardRepository.findByAccountIdAndStatus(
                accountId,
                CardStatus.ACTIVE
        );
    }

    /*
     * Ativa um cartão.
     *
     * Um cartão cancelado não pode ser ativado novamente.
     */
    public void activate(Card card) {

        if (card.getStatus() == CardStatus.CANCELLED) {
            throw new CardAlreadyCancelledError(
                    "A cancelled card cannot be activated."
            );
        }

        card.setStatus(CardStatus.ACTIVE);

        cardRepository.save(card);
    }

    /*
     * Bloqueia um cartão.
     *
     * Um cartão que já está bloqueado não pode ser bloqueado novamente.
     * Um cartão cancelado também não pode ser bloqueado.
     */
    public void block(Card card) {

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardAlreadyBlockedError(
                    "The card is already blocked."
            );
        }

        if (card.getStatus() == CardStatus.CANCELLED) {
            throw new CardAlreadyCancelledError(
                    "A cancelled card cannot be blocked."
            );
        }

        card.setStatus(CardStatus.BLOCKED);

        cardRepository.save(card);
    }

    /*
     * Cancela permanentemente um cartão.
     *
     * Depois de cancelado, o cartão não pode ser ativado novamente.
     */
    public void cancel(Card card) {

        if (card.getStatus() == CardStatus.CANCELLED) {
            throw new CardAlreadyCancelledError(
                    "The card is already cancelled."
            );
        }

        card.setStatus(CardStatus.CANCELLED);

        cardRepository.save(card);
    }

    /*
     * Valida se o cartão pode ser utilizado em uma transação.
     *
     * O cartão não pode ser utilizado caso esteja:
     * - bloqueado;
     * - cancelado;
     * - expirado.
     */
    public void validateCard(Card card) {

        // Verifica se o cartão está bloqueado.
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardBlockedError(
                    "Card is blocked."
            );
        }

        // Verifica se o cartão foi cancelado.
        if (card.getStatus() == CardStatus.CANCELLED) {
            throw new CardCancelledError(
                    "Card is cancelled."
            );
        }

        // Verifica se o cartão está marcado como expirado
        // ou se sua data de validade já passou.
        if (card.getStatus() == CardStatus.EXPIRED ||
                card.getExpirationDate().isBefore(LocalDate.now())) {

            throw new CardExpiredError(
                    "Card is expired."
            );
        }
    }

    /*
     * Verifica se o cartão está ativo.
     */
    public boolean isActive(Card card) {

        return card.getStatus() == CardStatus.ACTIVE;
    }

    /*
     * Verifica se o cartão é de crédito.
     */
    public boolean isCreditCard(Card card) {

        return card.getType() == CardType.CREDIT;
    }

    /*
     * Verifica se o cartão é de débito.
     */
    public boolean isDebitCard(Card card) {

        return card.getType() == CardType.DEBIT;
    }

    /*
     * Valida o tipo do cartão antes de sua criação.
     */
    private void validateType(CardType type) {

        if (type == null) {
            throw new InvalidCardTypeError(
                    "Card type cannot be null."
            );
        }
    }

    /*
     * Gera um número de cartão.
     *
     * Essa implementação é suficiente para o projeto acadêmico.
     * Em um sistema bancário real, seria necessário utilizar
     * um mecanismo seguro e controlado para geração dos números.
     */
    private String generateCardNumber() {

        return String.valueOf(
                1000000000000000L +
                        (long) (Math.random() * 9000000000000000L)
        );
    }
}