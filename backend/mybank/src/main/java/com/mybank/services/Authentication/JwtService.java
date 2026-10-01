package com.mybank.services.Authentication;

import com.mybank.entities.Authentication.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${security.jwt.expiration}")
    private long expiration;

    /*
     * Gera um Access Token JWT para o usuário autenticado.
     *
     * O token contém:
     * - ID do usuário;
     * - email;
     * - data de emissão;
     * - data de expiração.
     */
    public String generateAccessToken(User user) {

        Instant now = Instant.now();

        Instant expiresAt =
                now.plusSeconds(expiration);

        JwtClaimsSet claims =
                JwtClaimsSet.builder()

                        /*
                         * Identifica o usuário dentro do token.
                         */
                        .subject(
                                user.getId().toString()
                        )

                        /*
                         * Armazena o email do usuário
                         * como informação adicional.
                         */
                        .claim(
                                "email",
                                user.getEmail()
                        )

                        /*
                         * Define quando o token foi criado.
                         */
                        .issuedAt(now)

                        /*
                         * Define quando o token deixa de ser válido.
                         */
                        .expiresAt(expiresAt)

                        .build();

        return jwtEncoder
                .encode(
                        JwtEncoderParameters.from(
                                claims
                        )
                )
                .getTokenValue();
    }
}