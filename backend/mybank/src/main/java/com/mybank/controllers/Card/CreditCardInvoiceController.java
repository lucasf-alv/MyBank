package com.mybank.controllers.Card;

import com.mybank.dto.CreditCardInvoiceResponse;
import com.mybank.entities.Card.Card;
import com.mybank.entities.Card.CreditCardInvoice;
import com.mybank.services.Card.CardService;
import com.mybank.services.Card.CreditCardInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
        name = "Credit Card Invoices",
        description = "Endpoints for managing credit card invoices"
)
public class CreditCardInvoiceController {

    private final CreditCardInvoiceService invoiceService;
    private final CardService cardService;

    /*
     * Lista todas as faturas de um cartão.
     *
     * O cartão precisa pertencer ao usuário autenticado.
     */
    @GetMapping("/cards/{cardId}/invoices")
    @Operation(
            summary = "List card invoices",
            description = "Lists invoices belonging to a card owned by the authenticated user."
    )
    public ResponseEntity<List<CreditCardInvoiceResponse>> findByCard(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID cardId) {

        UUID userId = getAuthenticatedUserId(jwt);

        /*
         * Verifica se o cartão pertence ao usuário.
         */
        getOwnedCard(cardId, userId);

        List<CreditCardInvoiceResponse> invoices =
                invoiceService.findByCard(cardId)
                        .stream()
                        .map(CreditCardInvoiceResponse::from)
                        .toList();

        return ResponseEntity.ok(invoices);
    }

    /*
     * Consulta a fatura aberta de um cartão.
     *
     * Essa é a fatura que pode receber novas compras
     * realizadas no crédito.
     */
    @GetMapping("/cards/{cardId}/invoices/open")
    @Operation(
            summary = "Find open invoice",
            description = "Returns the open invoice of a card owned by the authenticated user."
    )
    public ResponseEntity<CreditCardInvoiceResponse> findOpenInvoice(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID cardId) {

        UUID userId = getAuthenticatedUserId(jwt);

        getOwnedCard(cardId, userId);

        CreditCardInvoice invoice =
                invoiceService.findOpenInvoice(cardId);

        return ResponseEntity.ok(
                CreditCardInvoiceResponse.from(invoice)
        );
    }

    /*
     * Consulta uma fatura pelo ID.
     *
     * Somente o proprietário do cartão associado
     * à fatura poderá consultar seus dados.
     */
    @GetMapping("/invoices/{invoiceId}")
    @Operation(
            summary = "Find invoice by ID",
            description = "Returns an invoice belonging to the authenticated user."
    )
    public ResponseEntity<CreditCardInvoiceResponse> findById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID invoiceId) {

        UUID userId = getAuthenticatedUserId(jwt);

        CreditCardInvoice invoice =
                getOwnedInvoice(invoiceId, userId);

        return ResponseEntity.ok(
                CreditCardInvoiceResponse.from(invoice)
        );
    }

    /*
     * Fecha uma fatura e cria a próxima fatura do cartão.
     *
     * O serviço já verifica se a data de fechamento
     * foi atingida.
     *
     * A resposta contém a fatura que acabou de ser fechada,
     * e não a próxima fatura criada.
     */
    @PatchMapping("/invoices/{invoiceId}/close")
    @Operation(
            summary = "Close an invoice",
            description = "Closes an eligible invoice and creates the next invoice."
    )
    public ResponseEntity<CreditCardInvoiceResponse> close(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID invoiceId) {

        UUID userId = getAuthenticatedUserId(jwt);

        CreditCardInvoice invoice =
                getOwnedInvoice(invoiceId, userId);

        /*
         * O serviço fecha a fatura atual e cria
         * automaticamente a próxima.
         */
        invoiceService.close(invoice);

        /*
         * Consulta novamente a fatura original para
         * retornar seus dados já atualizados.
         */
        CreditCardInvoice closedInvoice =
                invoiceService.findById(invoiceId);

        return ResponseEntity.ok(
                CreditCardInvoiceResponse.from(closedInvoice)
        );
    }

    /*
     * Marca uma fatura como vencida.
     *
     * Para ser elegível, a fatura precisa estar fechada
     * e sua data de vencimento precisa ter passado.
     *
     * Este endpoint é útil para os testes do projeto.
     * Em produção, essa transição normalmente seria
     * executada por um processo agendado.
     */
    @PatchMapping("/invoices/{invoiceId}/overdue")
    @Operation(
            summary = "Mark invoice as overdue",
            description = "Marks a closed invoice as overdue after its due date."
    )
    public ResponseEntity<CreditCardInvoiceResponse> markAsOverdue(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID invoiceId) {

        UUID userId = getAuthenticatedUserId(jwt);

        CreditCardInvoice invoice =
                getOwnedInvoice(invoiceId, userId);

        invoiceService.markAsOverdue(invoice);

        CreditCardInvoice updatedInvoice =
                invoiceService.findById(invoiceId);

        return ResponseEntity.ok(
                CreditCardInvoiceResponse.from(updatedInvoice)
        );
    }

    /*
     * Realiza o pagamento de uma fatura.
     *
     * O serviço:
     * - verifica o status da fatura;
     * - debita o valor da conta;
     * - registra a movimentação financeira;
     * - altera o status para PAID;
     * - cria a notificação e o registro de auditoria.
     */
    @PostMapping("/invoices/{invoiceId}/pay")
    @Operation(
            summary = "Pay an invoice",
            description = "Pays a closed or overdue invoice using the linked account balance."
    )
    public ResponseEntity<CreditCardInvoiceResponse> pay(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID invoiceId) {

        UUID userId = getAuthenticatedUserId(jwt);

        CreditCardInvoice invoice =
                getOwnedInvoice(invoiceId, userId);

        invoiceService.pay(invoice);

        CreditCardInvoice paidInvoice =
                invoiceService.findById(invoiceId);

        return ResponseEntity.ok(
                CreditCardInvoiceResponse.from(paidInvoice)
        );
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
     * Busca um cartão e verifica se pertence
     * ao usuário autenticado.
     */
    private Card getOwnedCard(
            UUID cardId,
            UUID userId) {

        Card card = cardService.findById(cardId);

        if (card.getAccount() == null
                || card.getAccount().getUser() == null
                || !card.getAccount().getUser()
                .getId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Card or invoice not found"
            );
        }

        return card;
    }

    /*
     * Busca uma fatura e valida a propriedade
     * do cartão associado.
     */
    private CreditCardInvoice getOwnedInvoice(
            UUID invoiceId,
            UUID userId) {

        CreditCardInvoice invoice =
                invoiceService.findById(invoiceId);

        getOwnedCard(
                invoice.getCard().getId(),
                userId
        );

        return invoice;
    }
}