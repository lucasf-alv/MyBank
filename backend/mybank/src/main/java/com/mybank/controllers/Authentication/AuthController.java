package com.mybank.controllers.Authentication;

import com.mybank.dto.AuthResponse;
import com.mybank.dto.LoginRequest;
import com.mybank.dto.RefreshTokenRequest;
import com.mybank.dto.RegisterRequest;
import com.mybank.dto.UserResponse;
import com.mybank.entities.Authentication.User;
import com.mybank.services.Authentication.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "Endpoints for user authentication"
)
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    /*
     * Cadastra um novo usuário.
     *
     * Os dados recebidos são validados e enviados
     * ao AuthService para executar o cadastro.
     *
     * Retorna somente os dados públicos do usuário.
     */
    @PostMapping("/register")
    @Operation(
            summary = "Register a user",
            description = "Creates a new user account."
    )
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = authService.register(request);

        UserResponse response =
                UserResponse.from(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * Autentica um usuário usando email e senha.
     *
     * Retorna o Access Token e o Refresh Token
     * após a autenticação bem-sucedida.
     */
    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Authenticates a user and returns both tokens."
    )
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response =
                authService.login(
                        request.email(),
                        request.password()
                );

        return ResponseEntity.ok(response);
    }

    /*
     * Renova os tokens usando um Refresh Token válido.
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh authentication tokens",
            description = "Revokes the current refresh token and issues new tokens."
    )
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response =
                authService.refresh(
                        request.refreshToken()
                );

        return ResponseEntity.ok(response);
    }

    /*
     * Realiza o logout revogando o Refresh Token.
     *
     * O endpoint não retorna o token nem outros dados
     * após a revogação.
     */
    @PostMapping("/logout")
    @Operation(
            summary = "Logout",
            description = "Revokes the supplied refresh token."
    )
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        authService.logout(
                request.refreshToken()
        );

        return ResponseEntity.noContent().build();
    }
}