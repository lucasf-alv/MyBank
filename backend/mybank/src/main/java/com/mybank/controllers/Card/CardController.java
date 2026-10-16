package com.mybank.controllers.Card;

import com.mybank.dto.CardCreateRequest;
import com.mybank.dto.CardResponse;
import com.mybank.entities.Account.Account;
import com.mybank.entities.Card.Card;
import com.mybank.services.Account.AccountService;
import com.mybank.services.Card.CardService;
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
@RequestMapping("/cards")
@RequiredArgsConstructor
@Tag(
        name = "Cards",
        description = "Endpoints for managing debit and credit cards"
)
public class CardController {

    private final CardService cardService;
    private final AccountService accountService;

    /*
     * Cria um cartão para uma conta do usuário autenticado.
     *
     * A propriedade da conta é verificada antes
     * de chamar o CardService.
     */
    @PostMapping
    @Operation(
            summary = "Create a card",
            description = "Creates a debit or credit card for an account owned by the authenticated user."
    )
    public ResponseEntity<CardResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CardCreateRequest request) {

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
         * O CardService gera o número, define a validade
         * e cria o cartão com status ACTIVE.
         *
         * O serviço também registra a notificação
         * e a ação no audit log.
         */
        Card card = cardService.create(
                account,
                request.holderName(),
                request.type()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CardResponse.from(card));
    }

    /*
     * Consulta um cartão pelo ID.
     *
     * O cartão somente será retornado se pertencer
     * a uma conta do usuário autenticado.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Find a card by ID",
            description = "Returns a card owned by the authenticated user."
    )
    public ResponseEntity<CardResponse> findById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = getAuthenticatedUserId(jwt);

        Card card = getOwnedCard(id, userId);

        return ResponseEntity.ok(
                CardResponse.from(card)
        );
    }

    /*
     * Lista os cartões associados a uma conta.
     *
     * A conta precisa pertencer ao usuário autenticado.
     */
    @GetMapping("/account/{accountId}")
    @Operation(
            summary = "List cards by account",
            description = "Lists cards belonging to an account owned by the authenticated user."
    )
    public ResponseEntity<List<CardResponse>> findByAccount(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId) {

        UUID userId = getAuthenticatedUserId(jwt);

        getOwnedAccount(accountId, userId);

        List<CardResponse> cards =
                cardService.findByAccount(accountId)
                        .stream()
                        .map(CardResponse::from)
                        .toList();

        return ResponseEntity.ok(cards);
    }

    /*
     * Lista somente os cartões ativos de uma conta.
     */
    @GetMapping("/account/{accountId}/active")
    @Operation(
            summary = "List active cards",
            description = "Lists active cards belonging to an account owned by the authenticated user."
    )
    public ResponseEntity<List<CardResponse>> findActiveCards(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId) {

        UUID userId = getAuthenticatedUserId(jwt);

        getOwnedAccount(accountId, userId);

        List<CardResponse> cards =
                cardService.findActiveCards(accountId)
                        .stream()
                        .map(CardResponse::from)
                        .toList();

        return ResponseEntity.ok(cards);
    }

    /*
     * Ativa um cartão.
     *
     * Somente o proprietário da conta associada
     * poderá executar a operação.
     */
    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "Activate a card",
            description = "Activates a card owned by the authenticated user."
    )
    public ResponseEntity<CardResponse> activate(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = getAuthenticatedUserId(jwt);

        Card card = getOwnedCard(id, userId);

        cardService.activate(card);

        /*
         * Busca novamente o cartão para retornar
         * o estado persistido após a operação.
         */
        Card updatedCard = cardService.findById(id);

        return ResponseEntity.ok(
                CardResponse.from(updatedCard)
        );
    }

    /*
     * Bloqueia um cartão.
     */
    @PatchMapping("/{id}/block")
    @Operation(
            summary = "Block a card",
            description = "Blocks a card owned by the authenticated user."
    )
    public ResponseEntity<CardResponse> block(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = getAuthenticatedUserId(jwt);

        Card card = getOwnedCard(id, userId);

        cardService.block(card);

        Card updatedCard = cardService.findById(id);

        return ResponseEntity.ok(
                CardResponse.from(updatedCard)
        );
    }

    /*
     * Cancela um cartão.
     */
    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel a card",
            description = "Cancels a card owned by the authenticated user."
    )
    public ResponseEntity<CardResponse> cancel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = getAuthenticatedUserId(jwt);

        Card card = getOwnedCard(id, userId);

        cardService.cancel(card);

        Card updatedCard = cardService.findById(id);

        return ResponseEntity.ok(
                CardResponse.from(updatedCard)
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
     * Busca uma conta e verifica sua propriedade.
     */
    private Account getOwnedAccount(
            UUID accountId,
            UUID userId) {

        Account account =
                accountService.findById(accountId);

        if (account.getUser() == null
                || !account.getUser().getId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Account not found"
            );
        }

        return account;
    }

    /*
     * Busca um cartão e verifica se pertence
     * ao usuário autenticado.
     */
    private Card getOwnedCard(
            UUID cardId,
            UUID userId) {

        Card card = cardService.findById(cardId);

        if (card.getAccount() == null
                || card.getAccount().getUser() == null
                || !card.getAccount().getUser().getId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Card not found"
            );
        }

        return card;
    }
}