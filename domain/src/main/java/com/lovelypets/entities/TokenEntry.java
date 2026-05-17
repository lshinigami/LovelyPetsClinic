package com.lovelypets.entities;

import com.lovelypets.enums.TokenType;
import com.lovelypets.enums.UserType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Персистентное представление JWT-токена.
 * Хранится для возможности отзыва (revoke) без инвалидации секрета.
 */
@Entity
@Table(name = "token_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** JWT-строка (jti или полный токен — зависит от стратегии). Уникальна. */
    @Column(nullable = false, unique = true, length = 2048)
    private String value;

    /** ID владельца токена (client.id или staff.id). */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Тип пользователя — различаем таблицы. */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private TokenType tokenType;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;
}
