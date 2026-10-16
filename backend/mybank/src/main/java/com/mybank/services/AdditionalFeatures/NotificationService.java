package com.mybank.services.AdditionalFeatures;

import com.mybank.entities.AdditionalFeatures.Notification;
import com.mybank.entities.Authentication.User;
import com.mybank.exceptions.NotificationNotFoundError;
import com.mybank.exceptions.InvalidNotificationMessageError;
import com.mybank.exceptions.NotificationAlreadyReadError;
import com.mybank.repositories.AdditionalFeatures.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /*
     * Cria uma nova notificação para um usuário.
     *
     * A notificação é criada como não lida.
     */
    public Notification create(
            User user,
            String message) {

        validateMessage(message);

        Notification notification = new Notification();

        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUser(user);

        return notificationRepository.save(notification);
    }

    /*
     * Busca uma notificação pelo ID.
     *
     * Caso a notificação não exista, lança uma exceção
     * para ser tratada pelo GlobalExceptionHandler.
     */
    public Notification findById(UUID id) {

        return notificationRepository.findById(id)
                .orElseThrow(() ->
                        new NotificationNotFoundError(
                                "Notification not found: " + id
                        )
                );
    }

    /*
     * Busca todas as notificações de um usuário.
     */
    public List<Notification> findByUser(UUID userId) {

        return notificationRepository
                .findByUserId(userId);
    }

    /*
     * Marca uma notificação como lida.
     *
     * Uma notificação que já foi lida não pode ser marcada
     * como lida novamente.
     */
    public void markAsRead(
            Notification notification) {

        if (notification.isRead()) {
            throw new NotificationAlreadyReadError(
                    "Notification is already read"
            );
        }

        notification.setRead(true);

        notificationRepository.save(notification);
    }


    /*
     * Verifica se uma notificação está lida.
     */
    public boolean isRead(
            Notification notification) {

        return notification.isRead();
    }

    /*
     * Verifica se uma notificação ainda não foi lida.
     */
    public boolean isUnread(
            Notification notification) {

        return !notification.isRead();
    }

    /*
     * Valida a mensagem da notificação.
     *
     * A mensagem não pode ser nula ou vazia.
     */
    private void validateMessage(String message) {

        if (message == null || message.isBlank()) {
            throw new InvalidNotificationMessageError(
                    "Notification message cannot be null or empty"
            );
        }
    }
    @Transactional(readOnly = true)
    public List<Notification> findByUserId(UUID userId) {

        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> findUnreadByUserId(UUID userId) {

        return notificationRepository
                .findByUser_IdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Notification markAsRead(
            UUID notificationId,
            UUID userId) {

        Notification notification = notificationRepository
                .findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notification not found"
                ));

        notification.setRead(true);

        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {

        List<Notification> notifications =
                notificationRepository
                        .findByUser_IdAndReadFalseOrderByCreatedAtDesc(
                                userId
                        );

        notifications.forEach(
                notification -> notification.setRead(true)
        );

        notificationRepository.saveAll(notifications);
    }
}