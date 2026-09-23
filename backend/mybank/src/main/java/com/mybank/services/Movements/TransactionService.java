package com.mybank.services.Movements;

import com.mybank.entities.Account.Account;
import com.mybank.entities.Movements.Transaction;
import com.mybank.entities.Movements.TransactionStatus;
import com.mybank.entities.Movements.TransactionType;
import com.mybank.exceptions.InvalidTransactionAmountError;
import com.mybank.exceptions.InvalidTransactionTypeError;
import com.mybank.exceptions.TransactionNotFoundError;
import com.mybank.repositories.Movements.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /*
     * Cria e registra uma nova transação financeira.
     *
     * Esse método é responsável apenas por registrar a movimentação
     * no histórico da conta. A alteração do saldo é responsabilidade
     * do AccountService.
     */
    public void create(
            Account account,
            TransactionType type,
            BigDecimal amount,
            String description,
            TransactionStatus status) {

        // Valida se o valor da transação é válido.
        validateAmount(amount);

        // Valida se o tipo da transação foi informado.
        validateType(type);

        Transaction transaction = new Transaction();

        // Define o tipo da movimentação, como CREDIT ou DEBIT.
        transaction.setType(type);

        // Define o valor da transação.
        transaction.setAmount(amount);

        // Define a descrição da movimentação.
        transaction.setDescription(description);

        // Armazena o saldo da conta após a operação.
        transaction.setBalanceAfter(account.getBalance());

        // Define o status da transação.
        transaction.setStatus(status);

        // Registra a data e hora da movimentação.
        transaction.setCreatedAt(LocalDateTime.now());

        // Associa a transação à conta.
        transaction.setAccount(account);

        transactionRepository.save(transaction);
    }

    /*
     * Busca uma transação pelo seu ID.
     *
     * Caso a transação não seja encontrada, lança
     * uma exceção TransactionNotFoundError.
     */
    public Transaction findById(UUID id) {

        return transactionRepository.findById(id)
                .orElseThrow(() ->
                        new TransactionNotFoundError(
                                "Transaction not found: " + id
                        )
                );
    }

    /*
     * Retorna todas as transações de uma determinada conta.
     */
    public List<Transaction> findByAccount(UUID accountId) {

        return transactionRepository.findByAccountId(accountId);
    }

    /*
     * Retorna as transações de uma conta dentro de um período
     * específico.
     *
     * O período é definido por uma data inicial e uma data final.
     */
    public List<Transaction> findByAccountAndPeriod(
            UUID accountId,
            LocalDateTime start,
            LocalDateTime end) {

        return transactionRepository
                .findByAccountIdAndCreatedAtBetween(
                        accountId,
                        start,
                        end
                );
    }

    /*
     * Registra uma entrada de dinheiro na conta.
     *
     * A movimentação é registrada como CREDIT e
     * começa com status COMPLETED.
     */
    public void recordCredit(
            Account account,
            BigDecimal amount,
            String description) {

        create(
                account,
                TransactionType.CREDIT,
                amount,
                description,
                TransactionStatus.COMPLETED
        );
    }

    /*
     * Registra uma saída de dinheiro da conta.
     *
     * A movimentação é registrada como DEBIT e
     * começa com status COMPLETED.
     */
    public void recordDebit(
            Account account,
            BigDecimal amount,
            String description) {

        create(
                account,
                TransactionType.DEBIT,
                amount,
                description,
                TransactionStatus.COMPLETED
        );
    }

    /*
     * Valida se o valor da transação é válido.
     *
     * Valores nulos, zero ou negativos não são permitidos.
     */
    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidTransactionAmountError(
                    "Transaction amount must be greater than zero."
            );
        }
    }

    /*
     * Valida se o tipo da transação foi informado.
     */
    private void validateType(TransactionType type) {

        if (type == null) {
            throw new InvalidTransactionTypeError(
                    "Transaction type cannot be null."
            );
        }
    }
}