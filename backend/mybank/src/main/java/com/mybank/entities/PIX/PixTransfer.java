package com.mybank.entities.PIX;

import com.mybank.entities.Account.Account;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pix_transfer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PixTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @ManyToOne
    @JoinColumn(name = "destination_account_id", nullable = false)
    private Account destinationAccount;

    @ManyToOne
    @JoinColumn(name = "pix_key_id", nullable = false)
    private PixKey pixKey;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PixTransferStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}