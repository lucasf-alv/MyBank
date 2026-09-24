package com.mybank.repositories.PIX;

import com.mybank.entities.PIX.PixTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PixTransferRepository
        extends JpaRepository<PixTransfer, UUID> {

    List<PixTransfer> findBySourceAccountId(UUID accountId);

    List<PixTransfer> findByDestinationAccountId(UUID accountId);

    List<PixTransfer> findByPixKeyId(UUID pixKeyId);
}