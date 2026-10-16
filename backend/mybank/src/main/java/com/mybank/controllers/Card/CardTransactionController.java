package com.mybank.controllers.Card;

import com.mybank.dto.CardTransactionCreateRequest;
import com.mybank.dto.CardTransactionResponse;
import com.mybank.entities.Card.Card;
import com.mybank.entities.Card.CardTransaction;
import com.mybank.entities.Card.CreditCardInvoice;
import com.mybank.services.Card.CardService;
import com.mybank.services.Card.CardTransactionService;
import com.mybank.services.Card.CreditCardInvoiceService;
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
@RequiredArgsConstructor
@Tag(
        name = "Card Transactions",
        description = "Endpoints for card purchases and transaction history"
)
public class CardTransactionController {

    private final CardTransactionService cardTransactionService;
    private final CardService cardService;
    private final CreditCardInvoiceService invoiceService;

    /*
     * Registra uma compra realizada através de um cartão.
     *
     * O cartão precisa pertencer a uma conta
     * do usuário autenticado.
     *
     * O CardTransactionService determina o comportamento
     * conforme o tipo do cartão:
     * - débito: desconta o valor da conta;
     * - crédito: adiciona o valor à fatura aberta.
     */
    @PostMapping("/cards/{cardId}/transactions")
    @Operation(
            summary = "Create a card transaction",
            description = "Registers a purchase using a debit or credit card owned by the authenticated user."
    )
    public ResponseEntity<CardTransactionResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID cardId,
            @Valid @RequestBody CardTransactionCreateRequest request) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Busca o cartão e verifica se pertence
         * ao usuário autenticado.
         */
        Card card = getOwnedCard(cardId, userId);

        /*
         * Processa a compra utilizando o serviço
         * responsável pelas regras de negócio.
         */
        CardTransaction transaction =
                cardTransactionService.create(
                        card,
                        request.amount(),
                        request.merchant(),
                        request.description()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CardTransactionResponse.from(transaction));
    }

    /*
     * Consulta uma compra pelo ID.
     *
     * Somente o proprietário da conta associada
     * ao cartão pode consultar a transação.
     */
    @GetMapping("/card-transactions/{id}")
    @Operation(
            summary = "Find a card transaction by ID",
            description = "Returns a card transaction belonging to the authenticated user."
    )
    public ResponseEntity<CardTransactionResponse> findById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {

        UUID userId = getAuthenticatedUserId(jwt);

        CardTransaction transaction =
                cardTransactionService.findById(id);

        validateCardOwnership(
                transaction.getCard(),
                userId
        );

        return ResponseEntity.ok(
                CardTransactionResponse.from(transaction)
        );
    }

    /*
     * Lista as compras realizadas através de um cartão.
     *
     * O cartão precisa pertencer ao usuário autenticado.
     */
    @GetMapping("/cards/{cardId}/transactions")
    @Operation(
            summary = "List card transactions",
            description = "Lists purchases made using a card owned by the authenticated user."
    )
    public ResponseEntity<List<CardTransactionResponse>> findByCard(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID cardId) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Verifica a propriedade do cartão antes
         * de consultar as compras.
         */
        getOwnedCard(cardId, userId);

        List<CardTransactionResponse> transactions =
                cardTransactionService.findByCard(cardId)
                        .stream()
                        .map(CardTransactionResponse::from)
                        .toList();

        return ResponseEntity.ok(transactions);
    }

    /*
     * Lista as compras vinculadas a uma fatura.
     *
     * Somente o proprietário da conta associada
     * ao cartão pode consultar as compras da fatura.
     */
    @GetMapping("/invoices/{invoiceId}/transactions")
    @Operation(
            summary = "List invoice transactions",
            description = "Lists purchases belonging to an invoice owned by the authenticated user."
    )
    public ResponseEntity<List<CardTransactionResponse>> findByInvoice(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID invoiceId) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Busca a fatura e verifica se pertence
         * a uma conta do usuário autenticado.
         */
        CreditCardInvoice invoice =
                invoiceService.findById(invoiceId);

        validateCardOwnership(
                invoice.getCard(),
                userId
        );

        List<CardTransactionResponse> transactions =
                cardTransactionService.findByInvoice(invoiceId)
                        .stream()
                        .map(CardTransactionResponse::from)
                        .toList();

        return ResponseEntity.ok(transactions);
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
     * Busca um cartão e verifica se pertence
     * ao usuário autenticado.
     */
    private Card getOwnedCard(
            UUID cardId,
            UUID userId) {

        Card card = cardService.findById(cardId);

        validateCardOwnership(
                card,
                userId
        );

        return card;
    }

    /*
     * Valida a propriedade do cartão.
     *
     * Caso o cartão não pertença ao usuário,
     * retorna 404 sem revelar dados de terceiros.
     */
    private void validateCardOwnership(
            Card card,
            UUID userId) {

        if (card.getAccount() == null
                || card.getAccount().getUser() == null
                || !card.getAccount().getUser()
                .getId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Card or transaction not found"
            );
        }
    }
}