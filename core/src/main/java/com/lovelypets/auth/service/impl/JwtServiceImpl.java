package com.lovelypets.auth.service.impl;

import com.lovelypets.auth.Token;
import com.lovelypets.auth.TokenPair;
import com.lovelypets.auth.service.JwtService;
import com.lovelypets.enums.TokenType;
import com.lovelypets.enums.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Реализация JWT-сервиса на базе JJWT 0.12.x.
 *
 * <p>Claims в токене:
 * <ul>
 *   <li>{@code sub} — email пользователя</li>
 *   <li>{@code userId} — id в таблице clients/staff</li>
 *   <li>{@code userType} — CLIENT | STAFF</li>
 *   <li>{@code tokenType} — ACCESS | REFRESH</li>
 * </ul>
 */
@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private static final String CLAIM_USER_ID   = "userId";
    private static final String CLAIM_USER_TYPE = "userType";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";

    private final SecretKey secretKey;

    /** Время жизни access-токена в миллисекундах. */
    private final long accessExpirationMs;

    /** Время жизни refresh-токена в миллисекундах. */
    private final long refreshExpirationMs;

    public JwtServiceImpl(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-ms:900000}") long accessExpirationMs,    // 15 мин по умолчанию
            @Value("${jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs // 7 дней
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Override
    public TokenPair generateTokenPair(Long userId, String email, UserType userType) {
        Token access  = buildToken(userId, email, userType, TokenType.ACCESS,  accessExpirationMs);
        Token refresh = buildToken(userId, email, userType, TokenType.REFRESH, refreshExpirationMs);
        return new TokenPair(access, refresh);
    }

    @Override
    public Token generateAccessToken(Long userId, String email, UserType userType) {
        return buildToken(userId, email, userType, TokenType.ACCESS, accessExpirationMs);
    }

    @Override
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token); // бросит исключение если невалиден
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    // ─────────────────────────── helpers ───────────────────────────

    private Token buildToken(Long userId, String email, UserType userType,
                             TokenType tokenType, long expirationMs) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusNanos(expirationMs * 1_000_000L);

        Date issuedDate  = toDate(now);
        Date expiredDate = toDate(expiresAt);

        String value = Jwts.builder()
                .subject(email)
                .claim(CLAIM_USER_ID,    userId)
                .claim(CLAIM_USER_TYPE,  userType.name())
                .claim(CLAIM_TOKEN_TYPE, tokenType.name())
                .issuedAt(issuedDate)
                .expiration(expiredDate)
                .signWith(secretKey)
                .compact();

        return new Token(value, userId, tokenType, now, expiresAt);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Вспомогательный метод: LocalDateTime → java.util.Date (нужен для JJWT). */
    private Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    // ─────────── Публичные методы для извлечения claims ──────────────

    public Long extractUserId(String token) {
        return parseClaims(token).get(CLAIM_USER_ID, Long.class);
    }

    public UserType extractUserType(String token) {
        return UserType.valueOf(parseClaims(token).get(CLAIM_USER_TYPE, String.class));
    }

    public TokenType extractTokenType(String token) {
        return TokenType.valueOf(parseClaims(token).get(CLAIM_TOKEN_TYPE, String.class));
    }
}
