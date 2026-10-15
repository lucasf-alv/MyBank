package com.mybank.controllers.Movements;

import com.mybank.dto.TransferCreateRequest;
import com.mybank.dto.TransferResponse;
import com.mybank.entities.Account.Account;
import com.mybank.entities.Movements.Transfer;
import com.mybank.services.Account.AccountService;
import com.mybank.services.Movements.TransferService;
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
@RequestMapping("/transfers")
@RequiredArgsConstructor
@Tag(
        name = "Transfers",
        description = "Endpoints for bank transfers"
)
public class TransferController {

    private final TransferService transferService;
    private final AccountService accountService;

    /*
     * Cria uma transferência entre duas contas.
     *
     * A conta de origem precisa pertencer ao usuário
     * autenticado.
     *
     * O TransferService executa as regras financeiras,
     * atualiza os saldos, registra as movimentações
     * e cria as notificações e os logs de auditoria.
     */
    @PostMapping
    @Operation(
            summary = "Create a transfer",
            description = "Transfers money between two bank accounts."
    )
    public ResponseEntity<TransferResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TransferCreateRequest request) {

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
         * Busca a conta de destino.
         *
         * O usuário pode transferir para outra conta,
         * mas não pode escolher uma conta de origem
         * que pertença a outra pessoa.
         */
        Account destinationAccount =
                accountService.findById(
                        request.destinationAccountId()
                );

        /*
         * Executa a transferência utilizando
         * o serviço responsável pelas regras de negócio.
         */
        Transfer transfer = transferService.create(
                sourceAccount,
                destinationAccount,
                request.amount(),
                request.description()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TransferResponse.from(transfer));
    }

    /*
     * Consulta uma transferência pelo ID.
     *
     * O usuário pode consultar transferências enviadas
     * ou recebidas, desde que seja proprietário de
     * pelo menos uma das contas envolvidas.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Find transfer by ID",
            description = "Returns a transfer involving the authenticated user."
    )
    public ResponseEntity<TransferResponse> findById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = getAuthenticatedUserId(jwt);

        Transfer transfer =
                transferService.findById(id);

        /*
         * Permite o acesso somente se o usuário
         * for proprietário da conta de origem
         * ou da conta de destino.
         */
        boolean ownsSourceAccount = belongsToUser(
                transfer.getSourceAccount(),
                userId
        );

        boolean ownsDestinationAccount = belongsToUser(
                transfer.getDestinationAccount(),
                userId
        );

        if (!ownsSourceAccount && !ownsDestinationAccount) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Transfer not found"
            );
        }

        return ResponseEntity.ok(
                TransferResponse.from(transfer)
        );
    }

    /*
     * Lista as transferências enviadas por uma conta.
     *
     * A conta precisa pertencer ao usuário autenticado.
     */
    @GetMapping("/accounts/{accountId}/sent")
    @Operation(
            summary = "List sent transfers",
            description = "Lists transfers sent from an account owned by the authenticated user."
    )
    public ResponseEntity<List<TransferResponse>> findSentTransfers(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Verifica a propriedade da conta antes
         * de consultar suas transferências.
         */
        getOwnedAccount(accountId, userId);

        List<TransferResponse> transfers =
                transferService.findSentTransfers(accountId)
                        .stream()
                        .map(TransferResponse::from)
                        .toList();

        return ResponseEntity.ok(transfers);
    }

    /*
     * Lista as transferências recebidas por uma conta.
     *
     * A conta precisa pertencer ao usuário autenticado.
     */
    @GetMapping("/accounts/{accountId}/received")
    @Operation(
            summary = "List received transfers",
            description = "Lists transfers received by an account owned by the authenticated user."
    )
    public ResponseEntity<List<TransferResponse>> findReceivedTransfers(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Verifica a propriedade da conta antes
         * de consultar suas transferências.
         */
        getOwnedAccount(accountId, userId);

        List<TransferResponse> transfers =
                transferService.findReceivedTransfers(accountId)
                        .stream()
                        .map(TransferResponse::from)
                        .toList();

        return ResponseEntity.ok(transfers);
    }

    /*
     * Extrai o UUID do usuário a partir do subject do JWT.
     */
    private UUID getAuthenticatedUserId(Jwt jwt) {

        return UUID.fromString(
                jwt.getSubject()
        );
    }

    /*
     * Busca uma conta e verifica sua propriedade.
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
     * Verifica se a conta pertence ao usuário informado.
     */
    private boolean belongsToUser(
            Account account,
            UUID userId) {

        return account != null
                && account.getUser() != null
                && account.getUser().getId().equals(userId);
    }
}