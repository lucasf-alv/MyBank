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

    public Account createAccount(User user, AccountType type) {
        Account account = new Account();

        account.setAccountNumber(generateAccountNumber());
        account.setBranch("0001");
        account.setBalance(BigDecimal.ZERO);
        account.setType(type);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        account.setUser(user);

        return accountRepository.save(account);
    }

    public Account findById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundError("Account not found: " + id)
                );
    }

    public List<Account> findByUser(UUID userId) {
        return accountRepository.findByUserId(userId);
    }

    public void credit(Account account, BigDecimal amount) {
        validateAmount(amount);
        validateAccount(account);

        account.setBalance(
                account.getBalance().add(amount)
        );

        account.setUpdatedAt(LocalDateTime.now());

        accountRepository.save(account);
    }

    public void debit(Account account, BigDecimal amount) {
        validateAmount(amount);
        validateAccount(account);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceError(
                    "Saldo insuficiente"
            );
        }

        account.setBalance(
                account.getBalance().subtract(amount)
        );

        account.setUpdatedAt(LocalDateTime.now());

        accountRepository.save(account);
    }

    public boolean hasSufficientBalance(
            Account account,
            BigDecimal amount) {

        validateAmount(amount);
        validateAccount(account);

        return account.getBalance().compareTo(amount) >= 0;
    }

    public void activate(Account account) {
        account.setStatus(AccountStatus.ACTIVE);
        account.setUpdatedAt(LocalDateTime.now());

        accountRepository.save(account);
    }

    public void block(Account account) {
        account.setStatus(AccountStatus.BLOCKED);
        account.setUpdatedAt(LocalDateTime.now());

        accountRepository.save(account);
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidAmountError(
                    "O saldo deve ser maior do que zero"
            );
        }
    }

    private void validateAccount(Account account) {

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new AccountBlockedError(
                    "Conta bloqueada"
            );
        }
    }

    private String generateAccountNumber() {
        return String.valueOf(
                10000000L +
                        (long) (Math.random() * 90000000L)
        );
    }
}