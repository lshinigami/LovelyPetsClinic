package com.lovelypets.mobileapi.auth.dto;

import com.lovelypets.auth.TokenPair;

import java.time.LocalDateTime;

/**
 * DTO ответа с парой токенов. Не возвращает внутренние поля типа userId.
 */
public record TokenPairResponse(
        String accessToken,
        String refreshToken,
        LocalDateTime accessExpiresAt,
        LocalDateTime refreshExpiresAt
) {
    public static TokenPairResponse from(TokenPair pair) {
        return new TokenPairResponse(
                pair.accessToken().getValue(),
                pair.refreshToken().getValue(),
                pair.accessToken().getExpiresAt(),
                pair.refreshToken().getExpiresAt()
        );
    }
}
