package com.mybank.entities.Authentication;

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
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column( name = "cpf" , nullable = false)
    private String cpf ;

    @Column ( name = "email" , nullable = false)
    private String email;

    @Column ( name = "password" , nullable = false )
    private String password;

    @Column( name = "phone" , nullable = false)
    private String phone ;

    @Column( name  = "birth_date" , nullable = false)
    private LocalDateTime birth_date;

    @Column( name = "status" , nullable = false)
    private String status ;

    @Column( name ="created_at" , nullable = false)
    private LocalDateTime created_at;

    @Column( name = "update_at" , nullable = false)
    private LocalDateTime update_at;

    @OneToMany(mappedBy = "user")
    private List<Account> accounts;

    @OneToMany(mappedBy = "user")
    private List<RefreshToken> refreshTokens;


}