package com.mybank.controllers.PIX;

import com.mybank.dto.PixTransferCreateRequest;
import com.mybank.dto.PixTransferResponse;
import com.mybank.entities.Account.Account;
import com.mybank.entities.PIX.PixKey;
import com.mybank.entities.PIX.PixTransfer;
import com.mybank.services.Account.AccountService;
import com.mybank.services.PIX.PixKeyService;
import com.mybank.services.PIX.PixTransferService;
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
@RequestMapping("/pix/transfers")
@RequiredArgsConstructor
@Tag(
        name = "PIX Transfers",
        description = "Endpoints for PIX transfers"
)
public class PixTransferController {

    private final PixTransferService pixTransferService;
    private final AccountService accountService;
    private final PixKeyService pixKeyService;

    /*
     * Realiza uma transferência utilizando uma chave PIX.
     *
     * A conta de origem precisa pertencer ao usuário
     * autenticado.
     *
     * O PixTransferService fica responsável por:
     * - validar a chave PIX;
     * - verificar o valor;
     * - validar as contas;
     * - atualizar os saldos;
     * - registrar as movimentações;
     * - criar notificações e logs de auditoria.
     */
    @PostMapping
    @Operation(
            summary = "Create a PIX transfer",
            description = "Transfers money to an account using a PIX key."
    )
    public ResponseEntity<PixTransferResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PixTransferCreateRequest request) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Busca a conta de origem e verifica
         * se pertence ao usuário autenticado.
         */
        Account sourceAccount = getOwnedAccount(
                request.sourceAccountId(),
                userId
        );

        /*
         * Executa a transferência.
         *
         * A conta de destino é descoberta pelo
         * PixTransferService por meio da chave PIX.
         */
        PixTransfer transfer = pixTransferService.create(
                sourceAccount,
                request.pixKey(),
                request.amount(),
                request.description()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PixTransferResponse.from(transfer));
    }

    /*
     * Consulta uma transferência PIX pelo ID.
     *
     * O usuário somente poderá consultar uma transferência
     * se for proprietário da conta de origem ou da conta
     * de destino.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Find a PIX transfer by ID",
            description = "Returns a PIX transfer involving the authenticated user."
    )
    public ResponseEntity<PixTransferResponse> findById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = getAuthenticatedUserId(jwt);

        PixTransfer transfer =
                pixTransferService.findById(id);

        boolean ownsSourceAccount = belongsToUser(
                transfer.getSourceAccount(),
                userId
        );

        boolean ownsDestinationAccount = belongsToUser(
                transfer.getDestinationAccount(),
                userId
        );

        /*
         * Impede a consulta de transferências
         * que não envolvam o usuário autenticado.
         */
        if (!ownsSourceAccount && !ownsDestinationAccount) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "PIX transfer not found"
            );
        }

        return ResponseEntity.ok(
                PixTransferResponse.from(transfer)
        );
    }

    /*
     * Lista as transferências PIX enviadas por uma conta.
     *
     * A conta precisa pertencer ao usuário autenticado.
     */
    @GetMapping("/accounts/{accountId}/sent")
    @Operation(
            summary = "List sent PIX transfers",
            description = "Lists PIX transfers sent from an account owned by the authenticated user."
    )
    public ResponseEntity<List<PixTransferResponse>> findSentTransfers(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Verifica a propriedade da conta.
         */
        getOwnedAccount(accountId, userId);

        List<PixTransferResponse> transfers =
                pixTransferService.findSentTransfers(accountId)
                        .stream()
                        .map(PixTransferResponse::from)
                        .toList();

        return ResponseEntity.ok(transfers);
    }

    /*
     * Lista as transferências PIX recebidas por uma conta.
     *
     * A conta precisa pertencer ao usuário autenticado.
     */
    @GetMapping("/accounts/{accountId}/received")
    @Operation(
            summary = "List received PIX transfers",
            description = "Lists PIX transfers received by an account owned by the authenticated user."
    )
    public ResponseEntity<List<PixTransferResponse>> findReceivedTransfers(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Verifica a propriedade da conta.
         */
        getOwnedAccount(accountId, userId);

        List<PixTransferResponse> transfers =
                pixTransferService.findReceivedTransfers(accountId)
                        .stream()
                        .map(PixTransferResponse::from)
                        .toList();

        return ResponseEntity.ok(transfers);
    }

    /*
     * Lista transferências associadas a uma chave PIX.
     *
     * Somente o proprietário da conta vinculada
     * à chave poderá consultar essas transferências.
     */
    @GetMapping("/key/{pixKeyId}")
    @Operation(
            summary = "List PIX transfers by key",
            description = "Lists transfers associated with a PIX key owned by the authenticated user."
    )
    public ResponseEntity<List<PixTransferResponse>> findByPixKey(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID pixKeyId) {

        UUID userId = getAuthenticatedUserId(jwt);

        PixKey pixKey =
                pixKeyService.findById(pixKeyId);

        /*
         * Verifica se a chave pertence a uma conta
         * do usuário autenticado.
         */
        if (!belongsToUser(pixKey.getAccount(), userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "PIX key not found"
            );
        }

        List<PixTransferResponse> transfers =
                pixTransferService.findByPixKey(pixKeyId)
                        .stream()
                        .map(PixTransferResponse::from)
                        .toList();

        return ResponseEntity.ok(transfers);
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
     * Busca uma conta e valida sua propriedade.
     */
    private Account getOwnedAccount(
            UUID accountId,
            UUID userId) {

        Account account =
                accountService.findById(accountId);

        if (!belongsToUser(account, userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Account not found"
            );
        }

        return account;
    }

    /*
     * Verifica se uma conta pertence ao usuário informado.
     */
    private boolean belongsToUser(
            Account account,
            UUID userId) {

        return account != null
                && account.getUser() != null
                && account.getUser().getId().equals(userId);
    }
}