package com.mybank.controllers.PIX;

import com.mybank.dto.PixKeyCreateRequest;
import com.mybank.dto.PixKeyResponse;
import com.mybank.entities.Account.Account;
import com.mybank.entities.PIX.PixKey;
import com.mybank.entities.Authentication.User;
import com.mybank.services.Account.AccountService;
import com.mybank.services.PIX.PixKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pix/keys")
@RequiredArgsConstructor
@Tag(
        name = "PIX Keys",
        description = "Endpoints for managing PIX keys"
)
public class PixKeyController {

    private final PixKeyService pixKeyService;
    private final AccountService accountService;

    /*
     * Cadastra uma nova chave PIX.
     *
     * A conta informada precisa pertencer ao usuário
     * autenticado.
     *
     * O PixKeyService fica responsável por validar
     * a chave e impedir duplicidades.
     */
    @PostMapping
    @Operation(
            summary = "Create a PIX key",
            description = "Creates a PIX key for an account owned by the authenticated user."
    )
    public ResponseEntity<PixKeyResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PixKeyCreateRequest request) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Busca a conta e verifica se pertence
         * ao usuário autenticado.
         */
        Account account = getOwnedAccount(
                request.accountId(),
                userId
        );

        /*
         * Cria a chave utilizando o serviço
         * responsável pelas regras de negócio.
         */
        PixKey pixKey = pixKeyService.create(
                account,
                request.key(),
                request.type()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PixKeyResponse.from(pixKey));
    }

    /*
     * Busca uma chave PIX pelo ID.
     *
     * A chave somente será retornada se pertencer
     * a uma conta do usuário autenticado.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Find a PIX key by ID",
            description = "Returns a PIX key owned by the authenticated user."
    )
    public ResponseEntity<PixKeyResponse> findById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = getAuthenticatedUserId(jwt);

        PixKey pixKey = pixKeyService.findById(id);

        /*
         * Impede que o usuário consulte chaves
         * pertencentes a outras pessoas.
         */
        validateAccountOwnership(
                pixKey.getAccount(),
                userId
        );

        return ResponseEntity.ok(
                PixKeyResponse.from(pixKey)
        );
    }

    /*
     * Lista as chaves PIX de uma conta.
     *
     * A conta precisa pertencer ao usuário autenticado.
     */
    @GetMapping("/account/{accountId}")
    @Operation(
            summary = "List PIX keys by account",
            description = "Lists PIX keys belonging to an account owned by the authenticated user."
    )
    public ResponseEntity<List<PixKeyResponse>> findByAccount(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Verifica a propriedade da conta antes
         * de consultar suas chaves PIX.
         */
        getOwnedAccount(accountId, userId);

        List<PixKeyResponse> pixKeys =
                pixKeyService.findByAccount(accountId)
                        .stream()
                        .map(PixKeyResponse::from)
                        .toList();

        return ResponseEntity.ok(pixKeys);
    }

    /*
     * Ativa uma chave PIX.
     *
     * Somente o proprietário da conta associada
     * pode executar essa operação.
     */
    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "Activate a PIX key",
            description = "Activates a PIX key owned by the authenticated user."
    )
    public ResponseEntity<PixKeyResponse> activate(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = getAuthenticatedUserId(jwt);

        PixKey pixKey = pixKeyService.findById(id);

        validateAccountOwnership(
                pixKey.getAccount(),
                userId
        );

        pixKeyService.activate(pixKey);

        return ResponseEntity.ok(
                PixKeyResponse.from(pixKey)
        );
    }

    /*
     * Desativa uma chave PIX.
     *
     * Somente o proprietário da conta associada
     * pode executar essa operação.
     */
    @PatchMapping("/{id}/deactivate")
    @Operation(
            summary = "Deactivate a PIX key",
            description = "Deactivates a PIX key owned by the authenticated user."
    )
    public ResponseEntity<PixKeyResponse> deactivate(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = getAuthenticatedUserId(jwt);

        PixKey pixKey = pixKeyService.findById(id);

        validateAccountOwnership(
                pixKey.getAccount(),
                userId
        );

        pixKeyService.deactivate(pixKey);

        return ResponseEntity.ok(
                PixKeyResponse.from(pixKey)
        );
    }

    /*
     * Obtém o UUID do usuário a partir do subject do JWT.
     */
    private UUID getAuthenticatedUserId(Jwt jwt) {

        return UUID.fromString(
                jwt.getSubject()
        );
    }

    /*
     * Busca uma conta e verifica se pertence
     * ao usuário autenticado.
     */
    private Account getOwnedAccount(
            UUID accountId,
            UUID userId) {

        Account account = accountService.findById(accountId);

        validateAccountOwnership(
                account,
                userId
        );

        return account;
    }

    /*
     * Valida a propriedade de uma conta.
     *
     * Retorna 404 quando a conta não pertence
     * ao usuário autenticado.
     */
    private void validateAccountOwnership(
            Account account,
            UUID userId) {

        User user = account.getUser();

        if (user == null || !user.getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "PIX key or account not found"
            );
        }
    }
}