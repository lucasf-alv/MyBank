package com.mybank.services.Card;

import com.mybank.entities.Account.Account;
import com.mybank.entities.Card.Card;
import com.mybank.entities.Card.CardTransaction;
import com.mybank.entities.Card.CreditCardInvoice;
import com.mybank.entities.Card.InvoiceStatus;
import com.mybank.exceptions.*;
import com.mybank.repositories.Card.CreditCardInvoiceRepository;
import com.mybank.services.Account.AccountService;
import com.mybank.services.AdditionalFeatures.AuditLogService;
import com.mybank.services.AdditionalFeatures.NotificationService;
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
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    /*
     * Cria uma nova fatura para um cartão.
     *
     * A fatura começa:
     * - com valor total igual a zero;
     * - com status OPEN;
     * - com uma data de fechamento;
     * - com uma data de vencimento.
     *
     * Essa fatura será utilizada para receber as compras
     * realizadas no cartão de crédito durante o ciclo.
     */
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

    /*
     * Busca uma fatura pelo seu ID.
     *
     * Caso a fatura não exista, lança uma exceção
     * para ser tratada pelo GlobalExceptionHandler.
     */
    public CreditCardInvoice findById(UUID id) {

        return invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new CreditCardInvoiceNotFoundError(
                                "Credit card invoice not found: " + id
                        )
                );
    }

    /*
     * Busca todas as faturas pertencentes a um determinado cartão.
     *
     * Um cartão pode possuir várias faturas ao longo do tempo.
     */
    public List<CreditCardInvoice> findByCard(UUID cardId) {

        return invoiceRepository.findByCardId(cardId);
    }

    /*
     * Busca a fatura atualmente aberta de um cartão.
     *
     * As compras realizadas no crédito devem ser adicionadas
     * à fatura que estiver com status OPEN.
     */
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

    /*
     * Adiciona uma compra à fatura.
     *
     * Primeiro verifica se a fatura ainda está aberta.
     * Depois soma o valor da compra ao total da fatura.
     */
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

    /*
     * Fecha uma fatura.
     *
     * Uma fatura fechada não pode mais receber novas compras.
     *
     * Depois de fechar a fatura atual, cria automaticamente
     * a próxima fatura do mesmo cartão.
     *
     * O @Transactional garante que o fechamento da fatura,
     * a criação da próxima fatura, a notificação e o audit log
     * façam parte da mesma transação.
     */
    @Transactional
    public CreditCardInvoice close(
            CreditCardInvoice invoice) {

        /*
         * Impede que uma fatura já fechada seja fechada novamente.
         */
        if (invoice.getStatus() == InvoiceStatus.CLOSED) {
            throw new InvoiceAlreadyClosedError(
                    "Invoice is already closed"
            );
        }

        /*
         * Uma fatura paga não pode ser fechada novamente.
         */
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvoiceAlreadyPaidError(
                    "Paid invoice cannot be closed"
            );
        }

        /*
         * Verifica se a data de fechamento já foi atingida.
         */
        if (LocalDate.now().isBefore(invoice.getClosingDate())) {
            throw new InvoiceNotReadyToCloseError(
                    "Invoice cannot be closed before the closing date"
            );
        }

        /*
         * Altera o status da fatura atual para CLOSED.
         */
        invoice.setStatus(InvoiceStatus.CLOSED);

        invoiceRepository.save(invoice);

        /*
         * Cria a próxima fatura do cartão.
         */
        CreditCardInvoice nextInvoice =
                createNextInvoice(invoice);

        /*
         * Obtém o usuário dono da conta associada ao cartão.
         */
        Account account =
                invoice.getCard().getAccount();

        /*
         * Notifica o usuário que a fatura foi fechada.
         */
        notificationService.create(
                account.getUser(),
                "Credit card invoice closed. "
                        + "Total amount: "
                        + invoice.getTotalAmount()
        );

        /*
         * Registra o fechamento da fatura no histórico
         * de auditoria.
         */
        auditLogService.create(
                account.getUser(),
                "CREDIT_CARD_INVOICE_CLOSED",
                "Credit card invoice "
                        + invoice.getId()
                        + " closed with total amount "
                        + invoice.getTotalAmount()
        );

        return nextInvoice;
    }

    /*
     * Marca uma fatura como vencida.
     *
     * É utilizada quando a data de vencimento passou
     * e a fatura ainda não foi paga.
     */
    @Transactional
    public void markAsOverdue(
            CreditCardInvoice invoice) {

        /*
         * Uma fatura que já foi paga não pode ficar vencida.
         */
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvoiceAlreadyPaidError(
                    "Paid invoice cannot be overdue"
            );
        }

        /*
         * A fatura só pode ser marcada como vencida
         * depois que a data de vencimento passar.
         */
        if (!LocalDate.now().isAfter(invoice.getDueDate())) {
            throw new InvoiceNotOverdueError(
                    "Invoice is not overdue yet"
            );
        }

        /*
         * Altera o status da fatura para OVERDUE.
         */
        invoice.setStatus(InvoiceStatus.OVERDUE);

        invoiceRepository.save(invoice);

        /*
         * Obtém o usuário dono da conta associada ao cartão.
         */
        Account account =
                invoice.getCard().getAccount();

        /*
         * Notifica o usuário que a fatura está vencida.
         */
        notificationService.create(
                account.getUser(),
                "Credit card invoice is overdue. "
                        + "Amount due: "
                        + invoice.getTotalAmount()
        );

        /*
         * Registra o vencimento da fatura no histórico
         * de auditoria.
         */
        auditLogService.create(
                account.getUser(),
                "CREDIT_CARD_INVOICE_OVERDUE",
                "Credit card invoice "
                        + invoice.getId()
                        + " became overdue with total amount "
                        + invoice.getTotalAmount()
        );
    }

    /*
     * Realiza o pagamento de uma fatura.
     *
     * O pagamento:
     *
     * 1. Verifica se a fatura pode ser paga;
     * 2. Obtém o cartão;
     * 3. Obtém a conta vinculada ao cartão;
     * 4. Debita o valor da conta;
     * 5. Registra uma Transaction;
     * 6. Marca a fatura como PAID;
     * 7. Cria uma notificação;
     * 8. Registra a ação no audit log.
     *
     * O @Transactional garante que todas essas operações
     * sejam tratadas como uma única transação.
     */
    @Transactional
    public void pay(
            CreditCardInvoice invoice) {

        /*
         * Impede que uma fatura já paga seja paga novamente.
         */
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvoiceAlreadyPaidError(
                    "Invoice is already paid"
            );
        }

        /*
         * A fatura precisa estar CLOSED ou OVERDUE
         * para poder ser paga.
         */
        if (invoice.getStatus() != InvoiceStatus.CLOSED &&
                invoice.getStatus() != InvoiceStatus.OVERDUE) {

            throw new InvoiceNotClosedError(
                    "Invoice must be closed before payment"
            );
        }

        /*
         * Obtém o cartão associado à fatura.
         */
        Card card = invoice.getCard();

        /*
         * Obtém a conta bancária associada ao cartão.
         */
        Account account = card.getAccount();

        /*
         * Obtém o valor total que deverá ser pago.
         */
        BigDecimal amount = invoice.getTotalAmount();

        /*
         * Debita o valor da fatura da conta.
         */
        accountService.debit(
                account,
                amount
        );

        /*
         * Registra o pagamento como uma movimentação
         * financeira na conta.
         */
        transactionService.recordDebit(
                account,
                amount,
                "Credit card invoice payment"
        );

        /*
         * Depois que o débito e o registro da movimentação
         * foram realizados, a fatura passa para PAID.
         */
        invoice.setStatus(InvoiceStatus.PAID);

        invoiceRepository.save(invoice);

        /*
         * Notifica o usuário sobre o pagamento da fatura.
         */
        notificationService.create(
                account.getUser(),
                "Credit card invoice paid successfully. "
                        + "Amount: "
                        + amount
        );

        /*
         * Registra o pagamento da fatura no histórico
         * de auditoria.
         */
        auditLogService.create(
                account.getUser(),
                "CREDIT_CARD_INVOICE_PAID",
                "Credit card invoice "
                        + invoice.getId()
                        + " paid with amount "
                        + amount
        );
    }

    /*
     * Verifica se a fatura está aberta.
     *
     * Retorna true quando o status é OPEN.
     */
    public boolean isOpen(
            CreditCardInvoice invoice) {

        return invoice.getStatus() == InvoiceStatus.OPEN;
    }

    /*
     * Verifica se a fatura está fechada.
     *
     * Retorna true quando o status é CLOSED.
     */
    public boolean isClosed(
            CreditCardInvoice invoice) {

        return invoice.getStatus() == InvoiceStatus.CLOSED;
    }

    /*
     * Verifica se a fatura já foi paga.
     *
     * Retorna true quando o status é PAID.
     */
    public boolean isPaid(
            CreditCardInvoice invoice) {

        return invoice.getStatus() == InvoiceStatus.PAID;
    }

    /*
     * Verifica se a fatura está vencida.
     *
     * Retorna true quando o status é OVERDUE.
     */
    public boolean isOverdue(
            CreditCardInvoice invoice) {

        return invoice.getStatus() == InvoiceStatus.OVERDUE;
    }

    /*
     * Cria a próxima fatura do cartão.
     *
     * A nova fatura começa com:
     * - valor zero;
     * - status OPEN;
     * - mesmo cartão da fatura anterior.
     *
     * As datas são calculadas a partir da fatura atual,
     * mantendo o ciclo mensal.
     */
    private CreditCardInvoice createNextInvoice(
            CreditCardInvoice currentInvoice) {

        CreditCardInvoice nextInvoice =
                new CreditCardInvoice();

        /*
         * Avança um mês em relação à data de fechamento
         * da fatura atual.
         */
        LocalDate nextClosingDate =
                currentInvoice.getClosingDate().plusMonths(1);

        /*
         * Avança um mês em relação à data de vencimento
         * da fatura atual.
         */
        LocalDate nextDueDate =
                currentInvoice.getDueDate().plusMonths(1);

        nextInvoice.setClosingDate(nextClosingDate);

        nextInvoice.setDueDate(nextDueDate);

        /*
         * A nova fatura começa sem nenhuma compra.
         */
        nextInvoice.setTotalAmount(
                BigDecimal.ZERO
        );

        /*
         * A nova fatura começa aberta para receber
         * as próximas compras.
         */
        nextInvoice.setStatus(
                InvoiceStatus.OPEN
        );

        nextInvoice.setCreatedAt(
                LocalDateTime.now()
        );

        /*
         * A nova fatura pertence ao mesmo cartão
         * da fatura anterior.
         */
        nextInvoice.setCard(
                currentInvoice.getCard()
        );

        return invoiceRepository.save(nextInvoice);
    }

    /*
     * Verifica se a fatura está aberta.
     *
     * Esse método é utilizado antes de adicionar
     * uma nova compra à fatura.
     *
     * Somente faturas OPEN podem receber novas compras.
     */
    private void validateOpenInvoice(
            CreditCardInvoice invoice) {

        if (invoice.getStatus() != InvoiceStatus.OPEN) {
            throw new InvoiceNotOpenError(
                    "Invoice is not open"
            );
        }
    }
}