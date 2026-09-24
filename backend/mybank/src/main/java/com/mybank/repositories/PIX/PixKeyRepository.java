package com.mybank.repositories.PIX;

import com.mybank.entities.PIX.PixKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PixKeyRepository
        extends JpaRepository<PixKey, UUID> {

    Optional<PixKey> findByKey(String key);

    List<PixKey> findByAccountId(UUID accountId);

    boolean existsByKey(String key);
}