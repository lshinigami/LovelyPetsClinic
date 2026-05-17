package com.lovelypets.auth;

import com.lovelypets.enums.TokenType;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Доменная модель токена. Не является JPA-сущностью.
 * Используется в сервисах и возвращается клиенту.
 */
@Getter
public class Token {

    private final String value;
    private final Long userId;
    private final TokenType type;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    private boolean revoked;

    public Token(String value, Long userId, TokenType type,
                 LocalDateTime issuedAt, LocalDateTime expiresAt) {
        this(value, userId, type, issuedAt, expiresAt, false);
    }

    public Token(String value, Long userId, TokenType type,
                 LocalDateTime issuedAt, LocalDateTime expiresAt, boolean revoked) {
        this.value = value;
        this.userId = userId;
        this.type = type;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

}
