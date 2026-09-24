package com.mybank.services.PIX;

import com.mybank.entities.Account.Account;
import com.mybank.entities.PIX.PixKey;
import com.mybank.entities.PIX.PixKeyStatus;
import com.mybank.entities.PIX.PixKeyType;
import com.mybank.exceptions.*;
import com.mybank.repositories.PIX.PixKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PixKeyService {

    private final PixKeyRepository pixKeyRepository;

    /*
     * Cria uma nova chave PIX para uma conta.
     *
     * Antes da criação:
     * - verifica se o tipo da chave foi informado;
     * - verifica se o valor da chave foi informado;
     * - verifica se a chave já está cadastrada.
     *
     * A nova chave começa com status ACTIVE.
     */
    public PixKey create(
            Account account,
            String key,
            PixKeyType type) {

        validateKey(key);
        validateType(type);

        if (pixKeyRepository.existsByKey(key)) {
            throw new PixKeyAlreadyExistsError(
                    "PIX key already exists"
            );
        }

        PixKey pixKey = new PixKey();

        pixKey.setKey(key);
        pixKey.setType(type);
        pixKey.setStatus(PixKeyStatus.ACTIVE);
        pixKey.setCreatedAt(LocalDateTime.now());
        pixKey.setAccount(account);

        return pixKeyRepository.save(pixKey);
    }

    /*
     * Busca uma chave PIX pelo ID.
     *
     * Caso a chave não exista, lança uma exceção
     * para ser tratada pelo GlobalExceptionHandler.
     */
    public PixKey findById(UUID id) {

        return pixKeyRepository.findById(id)
                .orElseThrow(() ->
                        new PixKeyNotFoundError(
                                "PIX key not found: " + id
                        )
                );
    }

    /*
     * Busca uma chave PIX pelo seu valor.
     *
     * O valor pode ser:
     * - CPF;
     * - CNPJ;
     * - e-mail;
     * - telefone;
     * - chave aleatória.
     */
    public PixKey findByKey(String key) {

        return pixKeyRepository.findByKey(key)
                .orElseThrow(() ->
                        new PixKeyNotFoundError(
                                "PIX key not found"
                        )
                );
    }

    /*
     * Busca todas as chaves PIX pertencentes
     * a uma determinada conta.
     */
    public List<PixKey> findByAccount(UUID accountId) {

        return pixKeyRepository.findByAccountId(accountId);
    }

    /*
     * Ativa uma chave PIX.
     *
     * Uma chave que já está ativa não precisa ser ativada novamente.
     */
    public void activate(PixKey pixKey) {

        if (pixKey.getStatus() == PixKeyStatus.ACTIVE) {
            throw new PixKeyAlreadyActiveError(
                    "PIX key is already active"
            );
        }

        pixKey.setStatus(PixKeyStatus.ACTIVE);

        pixKeyRepository.save(pixKey);
    }

    /*
     * Desativa uma chave PIX.
     *
     * Uma chave que já está inativa não pode ser desativada novamente.
     */
    public void deactivate(PixKey pixKey) {

        if (pixKey.getStatus() == PixKeyStatus.INACTIVE) {
            throw new PixKeyAlreadyInactiveError(
                    "PIX key is already inactive"
            );
        }

        pixKey.setStatus(PixKeyStatus.INACTIVE);

        pixKeyRepository.save(pixKey);
    }

    /*
     * Valida se uma chave PIX pode ser utilizada.
     *
     * Somente chaves ACTIVE podem receber transferências PIX.
     */
    public void validateKey(PixKey pixKey) {

        if (pixKey.getStatus() == PixKeyStatus.INACTIVE) {
            throw new PixKeyInactiveError(
                    "PIX key is inactive"
            );
        }
    }

    /*
     * Verifica se a chave está ativa.
     */
    public boolean isActive(PixKey pixKey) {

        return pixKey.getStatus() == PixKeyStatus.ACTIVE;
    }

    /*
     * Valida o valor da chave PIX.
     *
     * Uma chave não pode ser nula ou vazia.
     */
    private void validateKey(String key) {

        if (key == null || key.isBlank()) {
            throw new InvalidPixKeyError(
                    "PIX key cannot be null or empty"
            );
        }
    }

    /*
     * Valida o tipo da chave PIX.
     */
    private void validateType(PixKeyType type) {

        if (type == null) {
            throw new InvalidPixKeyTypeError(
                    "PIX key type cannot be null"
            );
        }
    }
}