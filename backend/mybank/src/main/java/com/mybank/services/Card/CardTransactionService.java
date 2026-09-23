package com.mybank.services.Card;

import com.mybank.entities.Account.Account;
import com.mybank.entities.Card.Card;
import com.mybank.entities.Card.CardTransaction;
import com.mybank.entities.Card.CardType;
import com.mybank.entities.Card.CreditCardInvoice;
import com.mybank.exceptions.CardTransactionNotFoundError;
import com.mybank.exceptions.InvalidCardTransactionAmountError;
import com.mybank.repositories.Card.CardTransactionRepository;
import com.mybank.services.Account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardTransactionService {

    private final CardTransactionRepository cardTransactionRepository;
    private final AccountService accountService;
    private final CardService cardService;
    private final CreditCardInvoiceService invoiceService;

    @Transactional
    public CardTransaction create(
            Card card,
            BigDecimal amount,
            String merchant,
            String description) {

        validateAmount(amount);

        cardService.validateCard(card);

        CardTransaction transaction = new CardTransaction();

        transaction.setAmount(amount);
        transaction.setMerchant(merchant);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setCard(card);

        if (card.getType() == CardType.DEBIT) {

            processDebitPurchase(
                    card,
                    amount
            );

        } else if (card.getType() == CardType.CREDIT) {

            processCreditPurchase(
                    card,
                    transaction
            );
        }

        return cardTransactionRepository.save(transaction);
    }

    public CardTransaction findById(UUID id) {

        return cardTransactionRepository.findById(id)
                .orElseThrow(() ->
                        new CardTransactionNotFoundError(
                                "Card transaction not found: " + id
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

        accountService.debit(
                account,
                amount
        );
    }

    private void processCreditPurchase(
            Card card,
            CardTransaction transaction) {

        CreditCardInvoice invoice =
                invoiceService.findOpenInvoice(
                        card.getId()
                );

        transaction.setInvoice(invoice);

        invoiceService.addTransaction(
                invoice,
                transaction
        );
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidCardTransactionAmountError(
                    "Card transaction amount must be greater than zero"
            );
        }
    }
}