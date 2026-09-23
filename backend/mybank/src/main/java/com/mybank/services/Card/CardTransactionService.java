package com.mybank.services.Card;

import com.mybank.entities.Account.Account;
import com.mybank.entities.Card.Card;
import com.mybank.entities.Card.CardTransaction;
import com.mybank.entities.Card.CardType;
import com.mybank.exceptions.CardBlockedError;
import com.mybank.exceptions.CardCancelledError;
import com.mybank.exceptions.CardExpiredError;
import com.mybank.exceptions.CardTransactionNotFoundError;
import com.mybank.exceptions.InvalidCardTransactionAmountError;
import com.mybank.repositories.Card.CardTransactionRepository;
import com.mybank.services.Account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardTransactionService {

    private final CardTransactionRepository cardTransactionRepository;
    private final AccountService accountService;
    private final CardService cardService;

    public CardTransaction create(
            Card card,
            BigDecimal amount,
            String merchant,
            String description) {

        validateAmount(amount);
        validateCard(card);

        CardTransaction transaction = new CardTransaction();

        transaction.setAmount(amount);
        transaction.setMerchant(merchant);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setCard(card);

        if (card.getType() == CardType.DEBIT) {
            processDebitPurchase(card, amount);
        }

        return cardTransactionRepository.save(transaction);
    }

    public CardTransaction findById(UUID id) {
        return cardTransactionRepository.findById(id)
                .orElseThrow(() ->
                        new CardTransactionNotFoundError(
                                "Transação não encontrada: " + id
                        )
                );
    }

    public List<CardTransaction> findByCard(UUID cardId) {
        return cardTransactionRepository.findByCardId(cardId);
    }

    public List<CardTransaction> findByInvoice(UUID invoiceId) {
        return cardTransactionRepository.findByInvoiceId(invoiceId);
    }

    private void processDebitPurchase(
            Card card,
            BigDecimal amount) {

        Account account = card.getAccount();

        accountService.debit(account, amount);
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidCardTransactionAmountError(
                    "A transação deve ser maio do que zero."
            );
        }
    }

    private void validateCard(Card card) {

        cardService.validateCard(card);
    }
}