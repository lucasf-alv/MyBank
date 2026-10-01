package com.mybank.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Base64;

@Configuration
public class SecurityConfig {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    /*
     * Define como as requisições HTTP da aplicação
     * serão protegidas.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        /*
         * Configuração da autorização dos endpoints.
         */
        http.authorizeHttpRequests(authorize -> authorize

                /*
                 * Endpoints públicos utilizados
                 * durante a autenticação.
                 */
                .requestMatchers(
                        "/auth/register",
                        "/auth/login",
                        "/auth/refresh"
                ).permitAll()

                /*
                 * Qualquer outro endpoint exige
                 * um JWT válido.
                 */
                .anyRequest().authenticated()
        );

        /*
         * A aplicação não utiliza sessão HTTP.
         */
        http.sessionManagement(session ->
                session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                )
        );

        /*
         * Configura a validação dos Bearer Tokens JWT.
         */
        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> {})
        );

        /*
         * A autenticação será feita através
         * do Bearer Token.
         */
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        /*
         * API stateless utilizando Bearer Token.
         */
        http.csrf(csrf -> csrf.disable());

        return http.build();
    }

    /*
     * Define o componente responsável pelo hash
     * das senhas dos usuários.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    /*
     * Cria a chave utilizada pelo JWT.
     *
     * A chave é armazenada como Base64
     * na variável de ambiente.
     */
    @Bean
    public SecretKey jwtSecretKey() {

        byte[] decodedKey =
                Base64.getDecoder().decode(jwtSecret);

        return new SecretKeySpec(
                decodedKey,
                "HmacSHA256"
        );
    }

    /*
     * Cria o componente responsável por assinar
     * os Access Tokens.
     */
    @Bean
    public JwtEncoder jwtEncoder(
            SecretKey jwtSecretKey) {

        return NimbusJwtEncoder
                .withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    /*
     * Cria o componente responsável por validar
     * os Access Tokens recebidos nas requisições.
     */
    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey) {

        return NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .build();
    }
}