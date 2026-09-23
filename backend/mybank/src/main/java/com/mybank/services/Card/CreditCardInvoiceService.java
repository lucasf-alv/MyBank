package com.mybank.services.Card;

import com.mybank.entities.Account.Account;
import com.mybank.entities.Card.Card;
import com.mybank.entities.Card.CardTransaction;
import com.mybank.entities.Card.CreditCardInvoice;
import com.mybank.entities.Card.InvoiceStatus;
import com.mybank.exceptions.*;
import com.mybank.repositories.Card.CreditCardInvoiceRepository;
import com.mybank.services.Account.AccountService;
import com.mybank.services.Movements.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditCardInvoiceService {

    private final CreditCardInvoiceRepository invoiceRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public CreditCardInvoice create(Card card) {

        CreditCardInvoice invoice = new CreditCardInvoice();

        invoice.setClosingDate(
                LocalDate.now().plusDays(20)
        );

        invoice.setDueDate(
                LocalDate.now().plusDays(30)
        );

        invoice.setTotalAmount(BigDecimal.ZERO);

        invoice.setStatus(InvoiceStatus.OPEN);

        invoice.setCreatedAt(LocalDateTime.now());

        invoice.setCard(card);

        return invoiceRepository.save(invoice);
    }

    public CreditCardInvoice findById(UUID id) {

        return invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new CreditCardInvoiceNotFoundError(
                                "Credit card invoice not found: " + id
                        )
                );
    }

    public List<CreditCardInvoice> findByCard(UUID cardId) {

        return invoiceRepository.findByCardId(cardId);
    }

    public CreditCardInvoice findOpenInvoice(UUID cardId) {

        return invoiceRepository
                .findByCardIdAndStatus(
                        cardId,
                        InvoiceStatus.OPEN
                )
                .orElseThrow(() ->
                        new CreditCardInvoiceNotFoundError(
                                "Open invoice not found for card: " + cardId
                        )
                );
    }

    public void addTransaction(
            CreditCardInvoice invoice,
            CardTransaction transaction) {

        validateOpenInvoice(invoice);

        BigDecimal newTotal =
                invoice.getTotalAmount()
                        .add(transaction.getAmount());

        invoice.setTotalAmount(newTotal);

        invoiceRepository.save(invoice);
    }

    @Transactional
    public CreditCardInvoice close(
            CreditCardInvoice invoice) {

        if (invoice.getStatus() == InvoiceStatus.CLOSED) {
            throw new InvoiceAlreadyClosedError(
                    "Invoice is already closed"
            );
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvoiceAlreadyPaidError(
                    "Paid invoice cannot be closed"
            );
        }

        invoice.setStatus(InvoiceStatus.CLOSED);

        invoiceRepository.save(invoice);

        return createNextInvoice(invoice);
    }

    public void markAsOverdue(
            CreditCardInvoice invoice) {

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvoiceAlreadyPaidError(
                    "Paid invoice cannot be overdue"
            );
        }

        invoice.setStatus(InvoiceStatus.OVERDUE);

        invoiceRepository.save(invoice);
    }

    @Transactional
    public void pay(
            CreditCardInvoice invoice) {

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvoiceAlreadyPaidError(
                    "Invoice is already paid"
            );
        }

        if (invoice.getStatus() != InvoiceStatus.CLOSED &&
                invoice.getStatus() != InvoiceStatus.OVERDUE) {

            throw new InvoiceNotClosedError(
                    "Invoice must be closed before payment"
            );
        }

        Card card = invoice.getCard();

        Account account = card.getAccount();

        BigDecimal amount = invoice.getTotalAmount();

        accountService.debit(
                account,
                amount
        );

        transactionService.recordDebit(
                account,
                amount,
                "Credit card invoice payment"
        );

        invoice.setStatus(InvoiceStatus.PAID);

        invoiceRepository.save(invoice);
    }

    public boolean isOpen(
            CreditCardInvoice invoice) {

        return invoice.getStatus() == InvoiceStatus.OPEN;
    }

    public boolean isClosed(
            CreditCardInvoice invoice) {

        return invoice.getStatus() == InvoiceStatus.CLOSED;
    }

    public boolean isPaid(
            CreditCardInvoice invoice) {

        return invoice.getStatus() == InvoiceStatus.PAID;
    }

    public boolean isOverdue(
            CreditCardInvoice invoice) {

        return invoice.getStatus() == InvoiceStatus.OVERDUE;
    }

    private CreditCardInvoice createNextInvoice(
            CreditCardInvoice currentInvoice) {

        CreditCardInvoice nextInvoice =
                new CreditCardInvoice();

        LocalDate nextClosingDate =
                currentInvoice.getClosingDate().plusMonths(1);

        LocalDate nextDueDate =
                currentInvoice.getDueDate().plusMonths(1);

        nextInvoice.setClosingDate(nextClosingDate);

        nextInvoice.setDueDate(nextDueDate);

        nextInvoice.setTotalAmount(
                BigDecimal.ZERO
        );

        nextInvoice.setStatus(
                InvoiceStatus.OPEN
        );

        nextInvoice.setCreatedAt(
                LocalDateTime.now()
        );

        nextInvoice.setCard(
                currentInvoice.getCard()
        );

        return invoiceRepository.save(nextInvoice);
    }

    private void validateOpenInvoice(
            CreditCardInvoice invoice) {

        if (invoice.getStatus() != InvoiceStatus.OPEN) {
            throw new InvoiceNotOpenError(
                    "Invoice is not open"
            );
        }
    }
}