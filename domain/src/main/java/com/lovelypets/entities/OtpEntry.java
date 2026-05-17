package com.lovelypets.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Временная запись OTP-кода, сгенерированного при регистрации клиента.
 * После успешной верификации запись удаляется (или помечается использованной).
 */
@Entity
@Table(name = "otp_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    /** Сам OTP-код (6 цифр). */
    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private boolean used;

    /** Время истечения срока действия кода. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
