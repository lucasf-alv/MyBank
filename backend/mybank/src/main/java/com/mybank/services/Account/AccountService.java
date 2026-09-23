package com.mybank.services.Account;

import com.mybank.entities.Account.Account;
import com.mybank.entities.Account.AccountStatus;
import com.mybank.entities.Account.AccountType;
import com.mybank.entities.Authentication.User;
import com.mybank.exceptions.AccountBlockedError;
import com.mybank.exceptions.AccountNotFoundError;
import com.mybank.exceptions.InsufficientBalanceError;
import com.mybank.exceptions.InvalidAmountError;
import com.mybank.repositories.Account.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    /*
     * Cria uma nova conta bancária para um usuário.
     *
     * A conta é criada com:
     * - número de conta gerado automaticamente;
     * - agência 0001;
     * - saldo inicial igual a zero;
     * - tipo de conta informado;
     * - status ACTIVE.
     */
    public Account createAccount(
            User user,
            AccountType type) {

        Account account = new Account();

        // Gera o número da conta.
        account.setAccountNumber(
                generateAccountNumber()
        );

        // Define a agência da conta.
        account.setBranch("0001");

        // Toda conta nova começa com saldo zero.
        account.setBalance(BigDecimal.ZERO);

        // Define o tipo da conta.
        account.setType(type);

        // Toda conta nova começa ativa.
        account.setStatus(AccountStatus.ACTIVE);

        // Registra a data de criação.
        account.setCreatedAt(LocalDateTime.now());

        // Registra a data da última atualização.
        account.setUpdatedAt(LocalDateTime.now());

        // Associa a conta ao usuário.
        account.setUser(user);

        return accountRepository.save(account);
    }

    /*
     * Busca uma conta pelo seu ID.
     *
     * Caso a conta não seja encontrada, lança
     * uma exceção AccountNotFoundError.
     */
    public Account findById(UUID id) {

        return accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundError(
                                "Account not found: " + id
                        )
                );
    }

    /*
     * Retorna todas as contas pertencentes a um usuário.
     */
    public List<Account> findByUser(UUID userId) {

        return accountRepository.findByUserId(userId);
    }

    /*
     * Adiciona um valor ao saldo da conta.
     *
     * Antes de realizar a operação, verifica:
     * - se o valor é válido;
     * - se a conta não está bloqueada.
     */
    public void credit(
            Account account,
            BigDecimal amount) {

        // Verifica se o valor é maior que zero.
        validateAmount(amount);

        // Verifica se a conta pode realizar operações.
        validateAccount(account);

        // Adiciona o valor ao saldo atual.
        account.setBalance(
                account.getBalance().add(amount)
        );

        // Atualiza a data da última alteração.
        account.setUpdatedAt(LocalDateTime.now());

        accountRepository.save(account);
    }

    /*
     * Remove um valor do saldo da conta.
     *
     * Antes de realizar a operação, verifica:
     * - se o valor é válido;
     * - se a conta não está bloqueada;
     * - se existe saldo suficiente.
     */
    public void debit(
            Account account,
            BigDecimal amount) {

        // Verifica se o valor é maior que zero.
        validateAmount(amount);

        // Verifica se a conta pode realizar operações.
        validateAccount(account);

        // Verifica se existe saldo suficiente para o débito.
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceError(
                    "Insufficient balance"
            );
        }

        // Subtrai o valor do saldo atual.
        account.setBalance(
                account.getBalance().subtract(amount)
        );

        // Atualiza a data da última alteração.
        account.setUpdatedAt(LocalDateTime.now());

        accountRepository.save(account);
    }

    /*
     * Verifica se a conta possui saldo suficiente
     * para realizar uma determinada operação.
     *
     * Esse método apenas realiza a verificação.
     * Ele não altera o saldo da conta.
     */
    public boolean hasSufficientBalance(
            Account account,
            BigDecimal amount) {

        // Verifica se o valor informado é válido.
        validateAmount(amount);

        // Verifica se a conta está disponível para operação.
        validateAccount(account);

        return account.getBalance().compareTo(amount) >= 0;
    }

    /*
     * Ativa uma conta.
     */
    public void activate(Account account) {

        account.setStatus(AccountStatus.ACTIVE);
        account.setUpdatedAt(LocalDateTime.now());

        accountRepository.save(account);
    }

    /*
     * Bloqueia uma conta.
     *
     * Uma conta bloqueada não poderá realizar operações
     * que alterem seu saldo.
     */
    public void block(Account account) {

        account.setStatus(AccountStatus.BLOCKED);
        account.setUpdatedAt(LocalDateTime.now());

        accountRepository.save(account);
    }

    /*
     * Valida se o valor informado é válido.
     *
     * Valores nulos, zero ou negativos não são permitidos.
     */
    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidAmountError(
                    "Amount must be greater than zero"
            );
        }
    }

    /*
     * Valida se a conta está disponível para realizar
     * operações financeiras.
     */
    private void validateAccount(Account account) {

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new AccountBlockedError(
                    "Account is blocked"
            );
        }
    }

    /*
     * Gera um número de conta com oito dígitos.
     *
     * Essa implementação é suficiente para o projeto acadêmico.
     * Em um sistema bancário real, seria necessário utilizar
     * uma estratégia segura para evitar números duplicados.
     */
    private String generateAccountNumber() {

        return String.valueOf(
                10000000L +
                        (long) (Math.random() * 90000000L)
        );
    }
}