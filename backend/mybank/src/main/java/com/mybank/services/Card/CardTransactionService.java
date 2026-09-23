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

    /*
     * Cria uma nova transação realizada através de um cartão.
     *
     * O comportamento depende do tipo do cartão:
     *
     * DÉBITO:
     * - valida o cartão;
     * - verifica o valor;
     * - debita o valor da conta;
     * - registra a transação.
     *
     * CRÉDITO:
     * - valida o cartão;
     * - verifica o valor;
     * - encontra a fatura aberta;
     * - adiciona a compra à fatura;
     * - registra a transação vinculada à fatura.
     *
     * O @Transactional garante que todas as operações
     * sejam revertidas caso alguma etapa apresente erro.
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
         *
         * O CardService verifica situações como:
         * - cartão bloqueado;
         * - cartão cancelado;
         * - cartão expirado.
         */
        cardService.validateCard(card);

        /*
         * Cria a entidade que representará a compra.
         */
        CardTransaction transaction = new CardTransaction();

        transaction.setAmount(amount);
        transaction.setMerchant(merchant);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setCard(card);

        /*
         * Se o cartão for de débito,
         * o valor é retirado imediatamente
         * da conta associada ao cartão.
         */
        if (card.getType() == CardType.DEBIT) {

            processDebitPurchase(
                    card,
                    amount
            );

            /*
             * Se o cartão for de crédito,
             * o valor não é retirado da conta imediatamente.
             *
             * A compra será adicionada à fatura aberta.
             */
        } else if (card.getType() == CardType.CREDIT) {

            processCreditPurchase(
                    card,
                    transaction
            );
        }

        /*
         * Depois de processar a compra,
         * salva a CardTransaction no banco.
         */
        return cardTransactionRepository.save(transaction);
    }

    /*
     * Busca uma transação de cartão pelo ID.
     *
     * Caso a transação não exista,
     * lança CardTransactionNotFoundError.
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
     * Busca todas as transações realizadas
     * através de um determinado cartão.
     */
    public List<CardTransaction> findByCard(UUID cardId) {

        return cardTransactionRepository.findByCardId(cardId);
    }

    /*
     * Busca todas as transações que pertencem
     * a uma determinada fatura.
     *
     * Isso permite consultar todas as compras
     * que compõem o valor da fatura.
     */
    public List<CardTransaction> findByInvoice(UUID invoiceId) {

        return cardTransactionRepository.findByInvoiceId(invoiceId);
    }

    /*
     * Processa uma compra realizada no débito.
     *
     * O cartão está associado a uma conta.
     * O AccountService é responsável por alterar
     * o saldo dessa conta.
     */
    private void processDebitPurchase(
            Card card,
            BigDecimal amount) {

        /*
         * Obtém a conta associada ao cartão.
         */
        Account account = card.getAccount();

        /*
         * Retira o valor da compra do saldo da conta.
         *
         * O AccountService também verifica:
         * - se a conta está bloqueada;
         * - se existe saldo suficiente;
         * - se o valor é válido.
         */
        accountService.debit(
                account,
                amount
        );
    }

    /*
     * Processa uma compra realizada no crédito.
     *
     * Diferentemente do débito, o saldo da conta
     * não é alterado neste momento.
     *
     * A compra é adicionada à fatura aberta
     * do cartão.
     */
    private void processCreditPurchase(
            Card card,
            CardTransaction transaction) {

        /*
         * Busca a fatura que está atualmente aberta
         * para receber novas compras.
         */
        CreditCardInvoice invoice =
                invoiceService.findOpenInvoice(
                        card.getId()
                );

        /*
         * Vincula a compra à fatura encontrada.
         */
        transaction.setInvoice(invoice);

        /*
         * Adiciona o valor da compra ao total da fatura.
         */
        invoiceService.addTransaction(
                invoice,
                transaction
        );
    }

    /*
     * Valida o valor da compra.
     *
     * O valor não pode:
     * - ser null;
     * - ser igual a zero;
     * - ser negativo.
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