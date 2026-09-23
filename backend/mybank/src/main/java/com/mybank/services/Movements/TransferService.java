package com.mybank.services.Movements;

import com.mybank.entities.Account.Account;
import com.mybank.entities.Movements.Transfer;
import com.mybank.entities.Movements.TranferStatus;
import com.mybank.exceptions.InvalidTransferAmountError;
import com.mybank.exceptions.TransferNotFoundError;
import com.mybank.exceptions.TransferSameAccountError;
import com.mybank.repositories.Movements.TransferRepository;
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
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @Transactional
    public Transfer create(
            Account sourceAccount,
            Account destinationAccount,
            BigDecimal amount,
            String description) {

        validateAmount(amount);
        validateAccounts(sourceAccount, destinationAccount);

        accountService.debit(sourceAccount, amount);

        accountService.credit(destinationAccount, amount);

        transactionService.recordDebit(
                sourceAccount,
                amount,
                description
        );

        transactionService.recordCredit(
                destinationAccount,
                amount,
                description
        );

        Transfer transfer = new Transfer();

        transfer.setAmount(amount);
        transfer.setDescription(description);
        transfer.setStatus(TranferStatus.COMPLETED);
        transfer.setCreatedAt(LocalDateTime.now());
        transfer.setSourceAccount(sourceAccount);
        transfer.setDestinationAccount(destinationAccount);

        return transferRepository.save(transfer);
    }

    public Transfer findById(UUID id) {
        return transferRepository.findById(id)
                .orElseThrow(() ->
                        new TransferNotFoundError(
                                "Transferencia não encontrada: " + id
                        )
                );
    }

    public List<Transfer> findSentTransfers(UUID accountId) {
        return transferRepository
                .findBySourceAccountId(accountId);
    }

    public List<Transfer> findReceivedTransfers(UUID accountId) {
        return transferRepository
                .findByDestinationAccountId(accountId);
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidTransferAmountError(
                    "Transferencia deve ser maio que zero."
            );
        }
    }

    private void validateAccounts(
            Account sourceAccount,
            Account destinationAccount) {

        if (sourceAccount.getId()
                .equals(destinationAccount.getId())) {

            throw new TransferSameAccountError(
                    "A conta de origem e de destino não podem ser iguais."
            );
        }
    }
}