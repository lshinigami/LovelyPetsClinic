package com.lovelypets.auth.service;

import com.lovelypets.auth.Token;
import com.lovelypets.auth.TokenPair;
import com.lovelypets.enums.UserType;

/**
 * Сервис работы с JWT: генерация, валидация, извлечение claims.
 */
public interface JwtService {

    /**
     * Генерирует пару токенов (access + refresh) для пользователя.
     *
     * @param userId   идентификатор пользователя в его таблице
     * @param email    email (sub claim)
     * @param userType тип пользователя (CLIENT / STAFF)
     * @return пара токенов
     */
    TokenPair generateTokenPair(Long userId, String email, UserType userType);

    /**
     * Генерирует один токен (используется при рефреше — выдаём новый ACCESS).
     */
    Token generateAccessToken(Long userId, String email, UserType userType);

    /**
     * Извлекает email (subject) из токена без верификации подписи.
     * Используется только для логирования / диагностики.
     */
    String extractEmail(String token);

    /**
     * Полная валидация токена: подпись + expiry.
     *
     * @return true если токен валиден
     */
    boolean isTokenValid(String token);
}
