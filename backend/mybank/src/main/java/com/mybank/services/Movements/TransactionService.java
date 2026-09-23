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

    public Transaction create(
            Account account,
            TransactionType type,
            BigDecimal amount,
            String description,
            TransactionStatus status) {

        validateAmount(amount);
        validateType(type);

        Transaction transaction = new Transaction();

        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setStatus(status);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setAccount(account);

        return transactionRepository.save(transaction);
    }

    public Transaction findById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() ->
                        new TransactionNotFoundError(
                                "Transação não encontrada: " + id
                        )
                );
    }

    public List<Transaction> findByAccount(UUID accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

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

    public Transaction recordCredit(
            Account account,
            BigDecimal amount,
            String description) {

        return create(
                account,
                TransactionType.CREDIT,
                amount,
                description,
                TransactionStatus.COMPLETED
        );
    }

    public Transaction recordDebit(
            Account account,
            BigDecimal amount,
            String description) {

        return create(
                account,
                TransactionType.DEBIT,
                amount,
                description,
                TransactionStatus.COMPLETED
        );
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidTransactionAmountError(
                    "Transação deve ser maior que zero."
            );
        }
    }

    private void validateType(TransactionType type) {

        if (type == null) {
            throw new InvalidTransactionTypeError(
                    "O tipo da transação não pode ser nulo."
            );
        }
    }
}