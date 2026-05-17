package com.lovelypets.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Хранит учётные данные клиента: email, SHA-256 хеш пароля и флаг верификации через OTP.
 * Отделена от Client, чтобы auth-данные не «текли» в бизнес-логику.
 */
@Entity
@Table(name = "client_credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Уникальный email — одновременно является логином. */
    @Column(nullable = false, unique = true)
    private String email;

    /** SHA-256 хеш пароля в hex-кодировке. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * true  — клиент подтвердил email через OTP и может логиниться.
     * false — регистрация ещё не завершена.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
