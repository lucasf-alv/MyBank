package com.mybank.entities.Account;

import com.mybank.entities.Authentication.User;
import com.mybank.entities.Card.Card;
import com.mybank.entities.Movements.Transaction;
import com.mybank.entities.Movements.Transfer;
import com.mybank.entities.PIX.PixKey;
import com.mybank.entities.PIX.PixTransfer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "branch", nullable = false)
    private String branch;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "account")
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "sourceAccount")
    private List<Transfer> sentTransfers;

    @OneToMany(mappedBy = "destinationAccount")
    private List<Transfer> receivedTransfers;

    @OneToMany(mappedBy = "account")
    private List<PixKey> pixKeys;

    @OneToMany(mappedBy = "sourceAccount")
    private List<PixTransfer> sentPixTransfers;

    @OneToMany(mappedBy = "destinationAccount")
    private List<PixTransfer> receivedPixTransfers;

    @OneToMany(mappedBy = "account")
    private List<Card> cards;
}