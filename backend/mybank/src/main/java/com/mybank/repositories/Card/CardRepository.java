package com.mybank.repositories.Card;

import com.mybank.entities.Card.Card;
import com.mybank.entities.Card.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    List<Card> findByAccountId(UUID accountId);

    List<Card> findByAccountIdAndStatus(
            UUID accountId,
            CardStatus status
    );
}