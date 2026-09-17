package com.mybank.entities.PIX;

import com.mybank.entities.Account.Account;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pix_key")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PixKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(name = "key", nullable = false)
    @Enumerated(EnumType.STRING)
    private PixKeyType key;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PixKeyType status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToMany(mappedBy = "pixKey")
    private List<PixTransfer> pixTransfers;

}