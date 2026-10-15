package com.mybank.controllers.Account;

import com.mybank.dto.AccountCreateRequest;
import com.mybank.dto.AccountResponse;
import com.mybank.entities.Account.Account;
import com.mybank.entities.Authentication.User;
import com.mybank.repositories.Authentication.UserRepository;
import com.mybank.services.Account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Accounts",
        description = "Endpoints for managing bank accounts"
)
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;

    /*
     * Cria uma conta bancária para o usuário autenticado.
     *
     * O ID do proprietário é extraído do JWT.
     * Dessa forma, o cliente não pode escolher
     * outro usuário enviando um userId no corpo.
     */
    @PostMapping
    @Operation(
            summary = "Create an account",
            description = "Creates a bank account for the authenticated user."
    )
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AccountCreateRequest request) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Busca o usuário associado ao JWT.
         */
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user not found"
                        )
                );

        /*
         * Cria a conta utilizando o serviço responsável
         * pelas regras de negócio.
         */
        Account account = accountService.createAccount(
                user,
                request.type()
        );

        /*
         * Retorna somente os dados públicos da conta.
         */
        AccountResponse response =
                AccountResponse.from(account);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * Lista as contas do usuário autenticado.
     *
     * Não aceita userId como parâmetro.
     * O proprietário é identificado pelo JWT.
     */
    @GetMapping
    @Operation(
            summary = "List accounts",
            description = "Lists the accounts belonging to the authenticated user."
    )
    public ResponseEntity<List<AccountResponse>> findMyAccounts(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Busca somente as contas pertencentes
         * ao usuário autenticado.
         */
        List<AccountResponse> accounts =
                accountService.findByUser(userId)
                        .stream()
                        .map(AccountResponse::from)
                        .toList();

        return ResponseEntity.ok(accounts);
    }

    /*
     * Busca uma conta pelo ID.
     *
     * Antes de retornar os dados, verifica se a conta
     * pertence ao usuário autenticado.
     *
     * Caso a conta pertença a outro usuário,
     * retorna 404 para não revelar sua existência.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Find account by ID",
            description = "Returns an account belonging to the authenticated user."
    )
    public ResponseEntity<AccountResponse> findById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = getAuthenticatedUserId(jwt);

        Account account = accountService.findById(id);

        /*
         * Impede a consulta de contas de outros usuários.
         */
        if (!belongsToUser(account, userId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                AccountResponse.from(account)
        );
    }

    /*
     * Consulta somente o saldo de uma conta.
     *
     * O saldo é retornado apenas quando a conta
     * pertence ao usuário autenticado.
     */
    @GetMapping("/{id}/balance")
    @Operation(
            summary = "Get account balance",
            description = "Returns the balance of an account owned by the authenticated user."
    )
    public ResponseEntity<BigDecimal> getBalance(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = getAuthenticatedUserId(jwt);

        Account account = accountService.findById(id);

        /*
         * Verifica a propriedade da conta antes
         * de expor informações financeiras.
         */
        if (!belongsToUser(account, userId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                account.getBalance()
        );
    }

    /*
     * Obtém o ID do usuário a partir do subject do JWT.
     *
     * O JwtService utiliza o UUID do usuário
     * como subject ao gerar o Access Token.
     */
    private UUID getAuthenticatedUserId(Jwt jwt) {

        return UUID.fromString(
                jwt.getSubject()
        );
    }

    /*
     * Verifica se a conta pertence ao usuário informado.
     */
    private boolean belongsToUser(
            Account account,
            UUID userId) {

        return account.getUser() != null
                && account.getUser().getId().equals(userId);
    }
}