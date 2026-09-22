package com.mybank.repositories.PIX;

import com.mybank.entities.PIX.PixKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PixKeyRepository extends JpaRepository<PixKey, UUID> {
}