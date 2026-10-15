package com.mybank.controllers.Movements;

import com.mybank.dto.TransactionResponse;
import com.mybank.entities.Account.Account;
import com.mybank.entities.Movements.Transaction;
import com.mybank.services.Account.AccountService;
import com.mybank.services.Movements.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(
        name = "Transactions",
        description = "Endpoints for querying account transactions"
)
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;

    /*
     * Consulta uma movimentação pelo ID.
     *
     * A movimentação somente será retornada
     * se pertencer a uma conta do usuário autenticado.
     */
    @GetMapping("/transactions/{id}")
    @Operation(
            summary = "Find transaction by ID",
            description = "Returns a transaction belonging to the authenticated user."
    )
    public ResponseEntity<TransactionResponse> findById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Busca a movimentação pelo ID.
         */
        Transaction transaction =
                transactionService.findById(id);

        /*
         * Verifica se a conta da movimentação
         * pertence ao usuário autenticado.
         */
        validateAccountOwnership(
                transaction.getAccount(),
                userId
        );

        return ResponseEntity.ok(
                TransactionResponse.from(transaction)
        );
    }

    /*
     * Lista as movimentações de uma conta.
     *
     * Antes da consulta, verifica se a conta pertence
     * ao usuário autenticado.
     */
    @GetMapping("/accounts/{accountId}/transactions")
    @Operation(
            summary = "List account transactions",
            description = "Lists transactions for an account owned by the authenticated user."
    )
    public ResponseEntity<List<TransactionResponse>> findByAccount(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Verifica a propriedade da conta antes
         * de consultar suas movimentações.
         */
        getOwnedAccount(accountId, userId);

        /*
         * Busca e converte as movimentações
         * em objetos de resposta.
         */
        List<TransactionResponse> transactions =
                transactionService.findByAccount(accountId)
                        .stream()
                        .map(TransactionResponse::from)
                        .toList();

        return ResponseEntity.ok(transactions);
    }

    /*
     * Consulta as movimentações de uma conta
     * dentro de um período.
     *
     * As datas devem ser informadas no formato ISO,
     * por exemplo: 2026-10-01T00:00:00.
     */
    @GetMapping("/accounts/{accountId}/transactions/period")
    @Operation(
            summary = "List transactions by period",
            description = "Lists account transactions between two dates."
    )
    public ResponseEntity<List<TransactionResponse>> findByAccountAndPeriod(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Verifica se a conta pertence ao usuário.
         */
        getOwnedAccount(accountId, userId);

        /*
         * Impede consultas com período invertido.
         */
        if (start.isAfter(end)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start date cannot be after end date"
            );
        }

        /*
         * Busca as movimentações dentro do período.
         */
        List<TransactionResponse> transactions =
                transactionService.findByAccountAndPeriod(
                                accountId,
                                start,
                                end
                        )
                        .stream()
                        .map(TransactionResponse::from)
                        .toList();

        return ResponseEntity.ok(transactions);
    }

    /*
     * Obtém o UUID do usuário autenticado
     * a partir do subject do JWT.
     */
    private UUID getAuthenticatedUserId(Jwt jwt) {

        return UUID.fromString(
                jwt.getSubject()
        );
    }

    /*
     * Busca uma conta e verifica se ela pertence
     * ao usuário autenticado.
     *
     * Retorna 404 se a conta não pertencer ao usuário,
     * evitando revelar a existência de contas alheias.
     */
    private Account getOwnedAccount(
            UUID accountId,
            UUID userId) {

        Account account =
                accountService.findById(accountId);

        validateAccountOwnership(
                account,
                userId
        );

        return account;
    }

    /*
     * Valida a propriedade de uma conta.
     */
    private void validateAccountOwnership(
            Account account,
            UUID userId) {

        if (account.getUser() == null
                || !account.getUser().getId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Account or transaction not found"
            );
        }
    }
}