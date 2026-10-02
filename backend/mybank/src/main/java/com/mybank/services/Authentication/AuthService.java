package com.mybank.services.Authentication;

import com.mybank.entities.Authentication.RefreshToken;
import com.mybank.entities.Authentication.User;
import com.mybank.entities.Authentication.UserStatus;
import com.mybank.exceptions.InvalidCredentialsError;
import com.mybank.exceptions.UserAlreadyExistsError;
import com.mybank.exceptions.UserBlockedError;
import com.mybank.repositories.Authentication.UserRepository;
import com.mybank.services.AdditionalFeatures.AuditLogService;
import com.mybank.services.AdditionalFeatures.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mybank.dto.AuthResponse;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    /*
     * Cadastra um novo usuário.
     *
     * Antes de salvar:
     * - verifica se o email já está em uso;
     * - verifica se o CPF já está em uso;
     * - transforma a senha em um hash;
     * - define o status inicial;
     * - registra as datas de criação e atualização.
     *
     * Depois do cadastro:
     * - envia uma notificação;
     * - registra a criação no audit log.
     */
    @Transactional
    public User register(User user) {

        /*
         * Verifica se já existe um usuário
         * utilizando o email informado.
         */
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsError(
                    "Email is already registered"
            );
        }

        /*
         * Verifica se já existe um usuário
         * utilizando o CPF informado.
         */
        if (userRepository.existsByCpf(user.getCpf())) {
            throw new UserAlreadyExistsError(
                    "CPF is already registered"
            );
        }

        /*
         * A senha nunca deve ser armazenada
         * diretamente no banco de dados.
         *
         * O PasswordEncoder transforma a senha
         * em um hash antes do armazenamento.
         */
        user.setPassword(
                passwordEncoder.encode(
                        user.getPassword()
                )
        );

        /*
         * Todo usuário novo começa ativo.
         */
        user.setStatus(
                UserStatus.ACTIVE
        );

        /*
         * Registra a data de criação.
         */
        user.setCreatedAt(
                LocalDateTime.now()
        );

        /*
         * Registra a data da última alteração.
         */
        user.setUpdatedAt(
                LocalDateTime.now()
        );

        /*
         * Salva o usuário no banco.
         */
        User savedUser =
                userRepository.save(user);

        /*
         * Informa ao usuário que o cadastro
         * foi concluído.
         */
        notificationService.create(
                savedUser,
                "User account created successfully"
        );

        /*
         * Registra a criação do usuário
         * no histórico de auditoria.
         */
        auditLogService.create(
                savedUser,
                "USER_REGISTERED",
                "User registered: "
                        + savedUser.getId()
        );

        return savedUser;
    }

    /*
     * Realiza o login de um usuário.
     *
     * O fluxo:
     * - busca o usuário pelo email;
     * - verifica o status;
     * - compara a senha informada com o hash armazenado;
     * - gera um Access Token;
     * - cria um Refresh Token;
     * - registra o login no audit log;
     * - envia uma notificação.
     */
    @Transactional
    public AuthResponse login(
            String email,
            String password) {

        /*
         * Busca o usuário pelo email.
         *
         * A mesma mensagem de erro é utilizada
         * para evitar diferenciar usuário inexistente
         * de senha incorreta.
         */
        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new InvalidCredentialsError(
                                        "Invalid email or password"
                                )
                        );

        /*
         * Verifica se o usuário está bloqueado.
         */
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new UserBlockedError(
                    "User is blocked"
            );
        }

        /*
         * Compara a senha recebida com o hash
         * armazenado no banco.
         */
        if (!passwordEncoder.matches(
                password,
                user.getPassword()
        )) {

            throw new InvalidCredentialsError(
                    "Invalid email or password"
            );
        }

        /*
         * Gera o Access Token JWT.
         */
        String accessToken =
                jwtService.generateAccessToken(user);

        /*
         * Cria um Refresh Token persistido no banco.
         */
        RefreshToken refreshToken =
                refreshTokenService.create(user);

        /*
         * Registra o login no histórico
         * de auditoria.
         */
        auditLogService.create(
                user,
                "USER_LOGIN",
                "User logged in successfully"
        );

        /*
         * Envia uma notificação sobre
         * o login realizado.
         */
        notificationService.create(
                user,
                "Login successful"
        );

        /*
         * Retorna os dois tokens para o cliente.
         */
        return new AuthResponse(
                accessToken,
                refreshToken.getToken()
        );
    }

    /*
     * Gera novos tokens utilizando um Refresh Token válido.
     *
     * O Refresh Token utilizado é revogado
     * e um novo é criado.
     *
     * Isso evita que o mesmo Refresh Token
     * continue válido indefinidamente.
     */
    @Transactional
    public AuthResponse refresh(
            String token) {

        /*
         * Localiza e valida o Refresh Token.
         */
        RefreshToken refreshToken =
                refreshTokenService.validate(token);

        /*
         * Obtém o usuário vinculado ao token.
         */
        User user =
                refreshToken.getUser();

        /*
         * Verifica se o usuário continua ativo.
         */
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new UserBlockedError(
                    "User is blocked"
            );
        }

        /*
         * Gera um novo Access Token.
         */
        String accessToken =
                jwtService.generateAccessToken(user);

        /*
         * Revoga o Refresh Token atual.
         */
        refreshTokenService.revoke(
                refreshToken
        );

        /*
         * Cria um novo Refresh Token.
         */
        RefreshToken newRefreshToken =
                refreshTokenService.create(user);

        /*
         * Registra a renovação da autenticação
         * no histórico de auditoria.
         */
        auditLogService.create(
                user,
                "TOKEN_REFRESHED",
                "Authentication tokens refreshed"
        );

        return new AuthResponse(
                accessToken,
                newRefreshToken.getToken()
        );
    }

    /*
     * Realiza o logout.
     *
     * O Access Token é stateless e expira
     * naturalmente.
     *
     * O Refresh Token é revogado imediatamente
     * para impedir novas autenticações.
     */
    @Transactional
    public void logout(
            String token) {

        /*
         * Localiza o Refresh Token.
         */
        RefreshToken refreshToken =
                refreshTokenService.findByToken(
                        token
                );

        /*
         * Obtém o usuário associado.
         */
        User user =
                refreshToken.getUser();

        /*
         * Revoga o Refresh Token.
         */
        refreshTokenService.revoke(
                refreshToken
        );

        /*
         * Registra o logout no histórico
         * de auditoria.
         */
        auditLogService.create(
                user,
                "USER_LOGOUT",
                "User logged out successfully"
        );
    }
}