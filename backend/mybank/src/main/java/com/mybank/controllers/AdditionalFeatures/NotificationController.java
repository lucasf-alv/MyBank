package com.mybank.controllers.AdditionalFeatures;

import com.mybank.dto.NotificationResponse;
import com.mybank.entities.AdditionalFeatures.Notification;
import com.mybank.services.AdditionalFeatures.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(
        name = "Notifications",
        description = "Endpoints for managing user notifications"
)
public class NotificationController {

    private final NotificationService notificationService;

    /*
     * Lista todas as notificações do usuário autenticado,
     * ordenadas da mais recente para a mais antiga.
     */
    @GetMapping
    @Operation(
            summary = "List notifications",
            description = "Lists notifications belonging to the authenticated user."
    )
    public ResponseEntity<List<NotificationResponse>> findMyNotifications(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = getAuthenticatedUserId(jwt);

        List<NotificationResponse> notifications =
                notificationService.findByUserId(userId)
                        .stream()
                        .map(NotificationResponse::from)
                        .toList();

        return ResponseEntity.ok(notifications);
    }

    /*
     * Lista somente as notificações ainda não lidas
     * do usuário autenticado.
     */
    @GetMapping("/unread")
    @Operation(
            summary = "List unread notifications",
            description = "Lists unread notifications belonging to the authenticated user."
    )
    public ResponseEntity<List<NotificationResponse>> findUnreadNotifications(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = getAuthenticatedUserId(jwt);

        List<NotificationResponse> notifications =
                notificationService.findUnreadByUserId(userId)
                        .stream()
                        .map(NotificationResponse::from)
                        .toList();

        return ResponseEntity.ok(notifications);
    }

    /*
     * Marca uma notificação específica como lida.
     *
     * O serviço verifica se a notificação pertence
     * ao usuário autenticado antes de alterá-la.
     */
    @PatchMapping("/{notificationId}/read")
    @Operation(
            summary = "Mark notification as read",
            description = "Marks a notification belonging to the authenticated user as read."
    )
    public ResponseEntity<NotificationResponse> markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID notificationId) {

        UUID userId = getAuthenticatedUserId(jwt);

        Notification notification =
                notificationService.markAsRead(
                        notificationId,
                        userId
                );

        return ResponseEntity.ok(
                NotificationResponse.from(notification)
        );
    }

    /*
     * Marca todas as notificações do usuário
     * autenticado como lidas.
     */
    @PatchMapping("/read-all")
    @Operation(
            summary = "Mark all notifications as read",
            description = "Marks all notifications belonging to the authenticated user as read."
    )
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = getAuthenticatedUserId(jwt);

        notificationService.markAllAsRead(userId);

        return ResponseEntity.noContent().build();
    }

    /*
     * Extrai o UUID do usuário a partir do subject do JWT.
     */
    private UUID getAuthenticatedUserId(Jwt jwt) {

        return UUID.fromString(
                jwt.getSubject()
        );
    }
}