package com.lovelypets.auth.service.impl;

import com.lovelypets.auth.Token;
import com.lovelypets.auth.TokenPair;
import com.lovelypets.auth.exception.AccountNotVerifiedException;
import com.lovelypets.auth.exception.InvalidCredentialsException;
import com.lovelypets.auth.exception.InvalidTokenException;
import com.lovelypets.auth.repository.ClientCredentialsRepository;
import com.lovelypets.auth.repository.StaffCredentialsRepository;
import com.lovelypets.auth.repository.TokenEntryRepository;
import com.lovelypets.auth.service.AuthService;
import com.lovelypets.auth.service.JwtService;
import com.lovelypets.auth.service.PasswordHasher;
import com.lovelypets.entities.ClientCredentials;
import com.lovelypets.entities.Staff;
import com.lovelypets.entities.StaffCredentials;
import com.lovelypets.entities.TokenEntry;
import com.lovelypets.enums.TokenType;
import com.lovelypets.enums.UserType;
import com.lovelypets.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация аутентификации для Client и Staff.
 *
 * <p>При каждом успешном логине предыдущие токены пользователя отзываются —
 * это предотвращает параллельные сессии (можно убрать, если нужна мультисессионность).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ClientCredentialsRepository clientCredentialsRepository;
    private final StaffCredentialsRepository staffCredentialsRepository;
    private final StaffRepository             staffRepository;
    private final TokenEntryRepository        tokenEntryRepository;
    private final PasswordHasher              passwordHasher;
    private final JwtService                  jwtService;
    private final JwtServiceImpl              jwtServiceImpl; // для extractUserId / extractUserType

    // ──────────────────── LOGIN CLIENT ────────────────────

    @Override
    @Transactional
    public TokenPair loginClient(String email, String rawPassword) {
        log.info("Login attempt for email={}", email);
        ClientCredentials credentials = clientCredentialsRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(rawPassword, credentials.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (!credentials.isVerified()) {
            throw new AccountNotVerifiedException();
        }

        // Отзываем старые токены
        revokeAllTokens(credentials.getId(), UserType.CLIENT);

        TokenPair pair = jwtService.generateTokenPair(credentials.getId(), email, UserType.CLIENT);
        persistTokenPair(pair, UserType.CLIENT);

        log.info("Client logged in: email={}", email);
        return pair;
    }

    // ──────────────────── LOGIN STAFF ────────────────────

    @Override
    @Transactional
    public TokenPair loginStaff(String email, String rawPassword) {
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!staff.getIsActive()) {
            throw new InvalidCredentialsException();
        }

        // Ищем в staff_credentials — не трогаем client_credentials
        StaffCredentials staffCredentials = staffCredentialsRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(rawPassword, staffCredentials.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        revokeAllTokens(staff.getId(), UserType.STAFF);

        TokenPair pair = jwtService.generateTokenPair(staff.getId(), email, UserType.STAFF);
        persistTokenPair(pair, UserType.STAFF);

        log.info("Staff logged in: email={}, role={}", email, staff.getRole());
        return pair;
    }

    // ──────────────────── REFRESH ────────────────────

    @Override
    @Transactional
    public TokenPair refresh(String refreshTokenValue) {
        if (!jwtService.isTokenValid(refreshTokenValue)) {
            throw new InvalidTokenException("Refresh token is expired or malformed");
        }

        // Проверяем что это именно REFRESH токен
        TokenType type = jwtServiceImpl.extractTokenType(refreshTokenValue);
        if (type != TokenType.REFRESH) {
            throw new InvalidTokenException("Token is not a refresh token");
        }

        // Проверяем что токен не отозван в БД
        TokenEntry tokenEntry = tokenEntryRepository.findByValue(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (tokenEntry.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        Long userId     = jwtServiceImpl.extractUserId(refreshTokenValue);
        String email    = jwtService.extractEmail(refreshTokenValue);
        UserType userType = jwtServiceImpl.extractUserType(refreshTokenValue);

        // Отзываем старые ACCESS токены (refresh оставляем, или отзываем тоже — rotation)
        tokenEntryRepository.revokeAllByUserIdAndUserTypeAndTokenType(userId, userType, TokenType.ACCESS);
        // Отзываем старый refresh (refresh token rotation — рекомендуется)
        tokenEntry.setRevoked(true);
        tokenEntryRepository.save(tokenEntry);

        // Выдаём новую пару
        TokenPair newPair = jwtService.generateTokenPair(userId, email, userType);
        persistTokenPair(newPair, userType);

        log.info("Tokens refreshed for userId={}, userType={}", userId, userType);
        return newPair;
    }

    // ──────────────────── LOGOUT ────────────────────

    @Override
    @Transactional
    public void logout(String accessTokenValue) {
        if (!jwtService.isTokenValid(accessTokenValue)) {
            // Всё равно пробуем отозвать по значению
            tokenEntryRepository.findByValue(accessTokenValue)
                    .ifPresent(t -> { t.setRevoked(true); tokenEntryRepository.save(t); });
            return;
        }

        Long userId      = jwtServiceImpl.extractUserId(accessTokenValue);
        UserType userType = jwtServiceImpl.extractUserType(accessTokenValue);

        revokeAllTokens(userId, userType);
        log.info("User logged out: userId={}, userType={}", userId, userType);
    }

    // ──────────────────── helpers ────────────────────

    private void revokeAllTokens(Long userId, UserType userType) {
        tokenEntryRepository.revokeAllByUserIdAndUserType(userId, userType);
    }

    private void persistTokenPair(TokenPair pair, UserType userType) {
        persistToken(pair.accessToken(), userType);
        persistToken(pair.refreshToken(), userType);
    }

    private void persistToken(Token token, UserType userType) {
        TokenEntry entry = TokenEntry.builder()
                .value(token.getValue())
                .userId(token.getUserId())
                .userType(userType)
                .tokenType(token.getType())
                .issuedAt(token.getIssuedAt())
                .expiresAt(token.getExpiresAt())
                .revoked(false)
                .build();
        tokenEntryRepository.save(entry);
    }
}
