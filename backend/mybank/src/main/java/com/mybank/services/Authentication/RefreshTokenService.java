package com.mybank.services.Authentication;

import com.mybank.entities.Authentication.RefreshToken;
import com.mybank.entities.Authentication.User;
import com.mybank.exceptions.RefreshTokenExpiredError;
import com.mybank.exceptions.RefreshTokenNotFoundError;
import com.mybank.exceptions.RefreshTokenRevokedError;
import com.mybank.repositories.Authentication.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.refresh-expiration}")
    private long refreshExpiration;

    /*
     * Cria um novo refresh token para o usuário.
     *
     * O refresh token é armazenado no banco para que
     * possa ser validado e revogado posteriormente.
     */
    @Transactional
    public RefreshToken create(User user) {

        RefreshToken refreshToken =
                new RefreshToken();

        /*
         * Gera um token aleatório que não contém
         * informações do usuário.
         */
        refreshToken.setToken(
                UUID.randomUUID().toString()
        );

        /*
         * Define a validade do refresh token.
         */
        refreshToken.setExpiresAt(
                LocalDateTime.now()
                        .plusSeconds(refreshExpiration)
        );

        /*
         * O token começa válido.
         */
        refreshToken.setRevoked(false);

        /*
         * Registra o momento da criação.
         */
        refreshToken.setCreatedAt(
                LocalDateTime.now()
        );

        /*
         * Associa o refresh token ao usuário.
         */
        refreshToken.setUser(user);

        return refreshTokenRepository.save(
                refreshToken
        );
    }

    /*
     * Busca um refresh token pelo seu valor.
     *
     * Caso o token não exista no banco,
     * lança RefreshTokenNotFoundError.
     */
    public RefreshToken findByToken(String token) {

        return refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() ->
                        new RefreshTokenNotFoundError(
                                "Refresh token not found"
                        )
                );
    }

    /*
     * Valida um refresh token.
     *
     * Verifica:
     * - se o token foi revogado;
     * - se o token ainda está dentro do prazo de validade.
     */
    public RefreshToken validate(
            String token) {

        RefreshToken refreshToken =
                findByToken(token);

        /*
         * Um token revogado não pode ser utilizado
         * novamente para gerar autenticação.
         */
        if (refreshToken.isRevoked()) {
            throw new RefreshTokenRevokedError(
                    "Refresh token has been revoked"
            );
        }

        /*
         * Verifica se o prazo de validade terminou.
         */
        if (!LocalDateTime.now()
                .isBefore(refreshToken.getExpiresAt())) {

            throw new RefreshTokenExpiredError(
                    "Refresh token has expired"
            );
        }

        return refreshToken;
    }

    /*
     * Revoga um refresh token.
     *
     * Depois da revogação, ele não poderá mais
     * ser utilizado para obter um novo Access Token.
     */
    @Transactional
    public void revoke(
            RefreshToken refreshToken) {

        if (refreshToken.isRevoked()) {
            return;
        }

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(
                refreshToken
        );
    }
}