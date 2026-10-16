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
import com.mybank.services.AdditionalFeatures.AuditLogService;
import com.mybank.services.AdditionalFeatures.NotificationService;
import com.mybank.services.Movements.TransactionService;
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
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final TransactionService transactionService;

    /*
     * Cria uma nova transação realizada através de um cartão.
     *
     * DÉBITO:
     * - valida o cartão e o valor;
     * - debita a conta;
     * - registra a movimentação no histórico da conta;
     * - registra a compra do cartão;
     * - cria a notificação;
     * - registra a ação na auditoria.
     *
     * CRÉDITO:
     * - valida o cartão e o valor;
     * - encontra a fatura aberta;
     * - adiciona a compra à fatura;
     * - registra a compra vinculada à fatura;
     * - cria a notificação;
     * - registra a ação na auditoria.
     *
     * Todas as operações participam da mesma transação,
     * permitindo o rollback em caso de falha.
     */
    @Transactional
    public CardTransaction create(
            Card card,
            BigDecimal amount,
            String merchant,
            String description) {

        /*
         * Verifica se o valor da compra é válido.
         */
        validateAmount(amount);

        /*
         * Verifica se o cartão pode ser utilizado.
         */
        cardService.validateCard(card);

        /*
         * Cria a entidade que representa a compra.
         */
        CardTransaction transaction = new CardTransaction();

        transaction.setAmount(amount);
        transaction.setMerchant(merchant);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setCard(card);

        /*
         * Processa a compra conforme o tipo do cartão.
         */
        if (card.getType() == CardType.DEBIT) {

            processDebitPurchase(
                    card,
                    amount,
                    description
            );

        } else if (card.getType() == CardType.CREDIT) {

            processCreditPurchase(
                    card,
                    transaction
            );
        }

        /*
         * Salva a compra no histórico do cartão.
         */
        CardTransaction savedTransaction =
                cardTransactionRepository.save(transaction);

        /*
         * Obtém a conta associada ao cartão.
         */
        Account account = card.getAccount();

        /*
         * Notifica o usuário sobre a compra.
         */
        notificationService.create(
                account.getUser(),
                "Card purchase of " + amount
                        + " at " + merchant
        );

        /*
         * Registra a ação na auditoria.
         */
        auditLogService.create(
                account.getUser(),
                "CARD_TRANSACTION_CREATED",
                "Card transaction of " + amount
                        + " at " + merchant
        );

        return savedTransaction;
    }

    /*
     * Busca uma transação de cartão pelo ID.
     */
    public CardTransaction findById(UUID id) {

        return cardTransactionRepository.findById(id)
                .orElseThrow(() ->
                        new CardTransactionNotFoundError(
                                "Card transaction not found: " + id
                        )
                );
    }

    /*
     * Busca as transações de um cartão.
     */
    public List<CardTransaction> findByCard(UUID cardId) {

        return cardTransactionRepository.findByCardId(cardId);
    }

    /*
     * Busca as transações vinculadas a uma fatura.
     */
    public List<CardTransaction> findByInvoice(UUID invoiceId) {

        return cardTransactionRepository.findByInvoiceId(invoiceId);
    }

    /*
     * Processa uma compra no débito.
     *
     * Além de debitar a conta, registra a movimentação
     * no histórico financeiro por meio do TransactionService.
     */
    private void processDebitPurchase(
            Card card,
            BigDecimal amount,
            String description) {

        /*
         * Obtém a conta associada ao cartão.
         */
        Account account = card.getAccount();

        /*
         * Debita o valor da compra.
         *
         * O AccountService mantém as validações existentes
         * de saldo e status da conta.
         */
        accountService.debit(
                account,
                amount
        );

        /*
         * Registra o débito no histórico da conta.
         *
         * Como o saldo já foi atualizado, a movimentação
         * poderá registrar o saldo posterior à compra.
         */
        transactionService.recordDebit(
                account,
                amount,
                description
        );
    }

    /*
     * Processa uma compra no crédito.
     *
     * O saldo da conta não é debitado neste momento.
     * A compra é adicionada à fatura aberta.
     */
    private void processCreditPurchase(
            Card card,
            CardTransaction transaction) {

        /*
         * Busca a fatura aberta do cartão.
         */
        CreditCardInvoice invoice =
                invoiceService.findOpenInvoice(
                        card.getId()
                );

        /*
         * Associa a compra à fatura.
         */
        transaction.setInvoice(invoice);

        /*
         * Atualiza o total da fatura.
         */
        invoiceService.addTransaction(
                invoice,
                transaction
        );
    }

    /*
     * Valida o valor da compra.
     *
     * O valor deve ser maior que zero.
     */
    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidCardTransactionAmountError(
                    "Card transaction amount must be greater than zero"
            );
        }
    }
}