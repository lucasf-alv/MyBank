package com.mybank.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}