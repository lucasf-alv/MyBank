package com.mybank.services.PIX;

import com.mybank.entities.Account.Account;
import com.mybank.entities.PIX.PixKey;
import com.mybank.entities.PIX.PixTransfer;
import com.mybank.entities.PIX.PixTransferStatus;
import com.mybank.exceptions.*;
import com.mybank.repositories.PIX.PixTransferRepository;
import com.mybank.services.Account.AccountService;
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
public class PixTransferService {

    private final PixTransferRepository pixTransferRepository;
    private final PixKeyService pixKeyService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    /*
     * Realiza uma transferência PIX.
     *
     * O processo:
     *
     * 1. Valida o valor da transferência;
     * 2. Valida a chave PIX;
     * 3. Obtém a conta de destino através da chave;
     * 4. Verifica se origem e destino são diferentes;
     * 5. Debita o valor da conta de origem;
     * 6. Credita o valor na conta de destino;
     * 7. Registra a saída na conta de origem;
     * 8. Registra a entrada na conta de destino;
     * 9. Cria o registro da transferência PIX.
     *
     * O @Transactional garante que todas as operações
     * sejam revertidas caso alguma etapa apresente erro.
     */
    @Transactional
    public PixTransfer create(
            Account sourceAccount,
            String pixKeyValue,
            BigDecimal amount,
            String description) {

        validateAmount(amount);

        /*
         * Busca a chave PIX utilizada como destino.
         *
         * O PixKeyService também verifica se a chave
         * está ativa e pode ser utilizada.
         */
        PixKey pixKey = pixKeyService.findByKey(pixKeyValue);

        pixKeyService.validateKey(pixKey);

        /*
         * Obtém a conta vinculada à chave PIX.
         */
        Account destinationAccount = pixKey.getAccount();

        /*
         * Impede que uma conta envie PIX para ela mesma.
         */
        validateAccounts(
                sourceAccount,
                destinationAccount
        );

        /*
         * Retira o valor da conta de origem.
         *
         * O AccountService também verifica:
         * - se o valor é válido;
         * - se a conta está bloqueada;
         * - se existe saldo suficiente.
         */
        accountService.debit(
                sourceAccount,
                amount
        );

        /*
         * Adiciona o valor na conta de destino.
         */
        accountService.credit(
                destinationAccount,
                amount
        );

        /*
         * Registra a saída do dinheiro na conta de origem.
         */
        transactionService.recordDebit(
                sourceAccount,
                amount,
                description
        );

        /*
         * Registra a entrada do dinheiro na conta de destino.
         */
        transactionService.recordCredit(
                destinationAccount,
                amount,
                description
        );

        /*
         * Cria o registro da transferência PIX.
         */
        PixTransfer pixTransfer = new PixTransfer();

        pixTransfer.setAmount(amount);
        pixTransfer.setDescription(description);
        pixTransfer.setStatus(PixTransferStatus.COMPLETED);
        pixTransfer.setCreatedAt(LocalDateTime.now());
        pixTransfer.setSourceAccount(sourceAccount);
        pixTransfer.setDestinationAccount(destinationAccount);
        pixTransfer.setPixKey(pixKey);

        return pixTransferRepository.save(pixTransfer);
    }

    /*
     * Busca uma transferência PIX pelo ID.
     *
     * Caso não encontre, lança uma exceção para ser
     * tratada pelo GlobalExceptionHandler.
     */
    public PixTransfer findById(UUID id) {

        return pixTransferRepository.findById(id)
                .orElseThrow(() ->
                        new PixTransferNotFoundError(
                                "PIX transfer not found: " + id
                        )
                );
    }

    /*
     * Busca todas as transferências PIX enviadas
     * por uma determinada conta.
     */
    public List<PixTransfer> findSentTransfers(
            UUID accountId) {

        return pixTransferRepository
                .findBySourceAccountId(accountId);
    }

    /*
     * Busca todas as transferências PIX recebidas
     * por uma determinada conta.
     */
    public List<PixTransfer> findReceivedTransfers(
            UUID accountId) {

        return pixTransferRepository
                .findByDestinationAccountId(accountId);
    }

    /*
     * Busca todas as transferências realizadas
     * utilizando uma determinada chave PIX.
     */
    public List<PixTransfer> findByPixKey(
            UUID pixKeyId) {

        return pixTransferRepository
                .findByPixKeyId(pixKeyId);
    }

    /*
     * Valida o valor da transferência.
     *
     * O valor precisa ser maior que zero.
     */
    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidPixTransferAmountError(
                    "PIX transfer amount must be greater than zero"
            );
        }
    }

    /*
     * Valida as contas envolvidas na transferência.
     *
     * A conta de origem não pode ser a mesma conta
     * vinculada à chave PIX de destino.
     */
    private void validateAccounts(
            Account sourceAccount,
            Account destinationAccount) {

        if (sourceAccount.getId()
                .equals(destinationAccount.getId())) {

            throw new PixTransferSameAccountError(
                    "Source and destination accounts cannot be the same"
            );
        }
    }
}