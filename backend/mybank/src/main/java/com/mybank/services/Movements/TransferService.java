package com.mybank.services.Movements;

import com.mybank.entities.Account.Account;
import com.mybank.entities.Movements.Transfer;
import com.mybank.entities.Movements.TransferStatus;
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

    /*
     * Cria uma transferência entre duas contas.
     *
     * O processo realiza:
     * - validação do valor;
     * - validação das contas;
     * - débito na conta de origem;
     * - crédito na conta de destino;
     * - registro da saída na conta de origem;
     * - registro da entrada na conta de destino;
     * - registro da transferência.
     *
     * O @Transactional garante que todas as operações
     * sejam revertidas caso alguma etapa apresente erro.
     */
    @Transactional
    public Transfer create(
            Account sourceAccount,
            Account destinationAccount,
            BigDecimal amount,
            String description) {

        // Valida se o valor da transferência é válido.
        validateAmount(amount);

        // Verifica se as contas de origem e destino são válidas.
        validateAccounts(
                sourceAccount,
                destinationAccount
        );

        // Remove o valor da conta de origem.
        accountService.debit(
                sourceAccount,
                amount
        );

        // Adiciona o valor na conta de destino.
        accountService.credit(
                destinationAccount,
                amount
        );

        // Registra a saída de dinheiro na conta de origem.
        transactionService.recordDebit(
                sourceAccount,
                amount,
                description
        );

        // Registra a entrada de dinheiro na conta de destino.
        transactionService.recordCredit(
                destinationAccount,
                amount,
                description
        );

        Transfer transfer = new Transfer();

        // Define o valor da transferência.
        transfer.setAmount(amount);

        // Define a descrição da transferência.
        transfer.setDescription(description);

        // Define a transferência como concluída.
        transfer.setStatus(TransferStatus.COMPLETED);

        // Registra a data e hora da transferência.
        transfer.setCreatedAt(LocalDateTime.now());

        // Define a conta que enviou o dinheiro.
        transfer.setSourceAccount(sourceAccount);

        // Define a conta que recebeu o dinheiro.
        transfer.setDestinationAccount(destinationAccount);

        return transferRepository.save(transfer);
    }

    /*
     * Busca uma transferência pelo seu ID.
     *
     * Caso a transferência não seja encontrada, lança
     * uma exceção TransferNotFoundError.
     */
    public Transfer findById(UUID id) {

        return transferRepository.findById(id)
                .orElseThrow(() ->
                        new TransferNotFoundError(
                                "Transfer not found: " + id
                        )
                );
    }

    /*
     * Retorna todas as transferências enviadas
     * por uma determinada conta.
     */
    public List<Transfer> findSentTransfers(UUID accountId) {

        return transferRepository
                .findBySourceAccountId(accountId);
    }

    /*
     * Retorna todas as transferências recebidas
     * por uma determinada conta.
     */
    public List<Transfer> findReceivedTransfers(UUID accountId) {

        return transferRepository
                .findByDestinationAccountId(accountId);
    }

    /*
     * Valida se o valor da transferência é válido.
     *
     * Valores nulos, zero ou negativos não são permitidos.
     */
    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidTransferAmountError(
                    "Transfer amount must be greater than zero."
            );
        }
    }

    /*
     * Valida se as contas de origem e destino são diferentes.
     *
     * Uma transferência não pode ser realizada entre
     * a mesma conta de origem e destino.
     */
    private void validateAccounts(
            Account sourceAccount,
            Account destinationAccount) {

        if (sourceAccount.getId()
                .equals(destinationAccount.getId())) {

            throw new TransferSameAccountError(
                    "Source and destination accounts cannot be the same."
            );
        }
    }
}