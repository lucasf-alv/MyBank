package com.mybank.repositories.Movements;

import com.mybank.entities.Movements.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    List<Transfer> findBySourceAccountId(UUID accountId);

    List<Transfer> findByDestinationAccountId(UUID accountId);
}