package com.lovelypets.mobileapi.auth;

import com.lovelypets.auth.TokenPair;
import com.lovelypets.auth.service.AuthService;
import com.lovelypets.auth.service.RegistrationService;
import com.lovelypets.mobileapi.auth.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер аутентификации и регистрации.
 *
 * <pre>
 * POST /api/v1/auth/register          — Шаг 1 регистрации (Client)
 * POST /api/v1/auth/verify-otp        — Шаг 2 регистрации (Client)
 * POST /api/v1/auth/login/client      — Логин клиента
 * POST /api/v1/auth/login/staff       — Логин сотрудника
 * POST /api/v1/auth/refresh           — Обновление токенов
 * POST /api/v1/auth/logout            — Выход
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, Login, Token management")
public class AuthController {

    private final RegistrationService registrationService;
    private final AuthService         authService;

    // ─────────────────── REGISTRATION ───────────────────

    /**
     * Шаг 1: клиент вводит email + password.
     * Сохраняем credentials и отправляем OTP на почту через Kafka.
     */
    @PostMapping("/register")
    @Operation(summary = "Initiate client registration (Step 1)")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        registrationService.initiateRegistration(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Шаг 2: клиент вводит OTP из письма.
     * При успехе аккаунт помечается как verified.
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP code (Step 2 of registration)")
    public ResponseEntity<Void> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        registrationService.verifyOtp(request.email(), request.otp());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    // ─────────────────── LOGIN ───────────────────

    @PostMapping("/login")
    @Operation(summary = "Login for Client")
    public ResponseEntity<TokenPairResponse> loginClient(@Valid @RequestBody LoginRequest request) {
        TokenPair pair = authService.loginClient(request.email(), request.password());
        return ResponseEntity.ok(TokenPairResponse.from(pair));
    }

    // ─────────────────── TOKEN MANAGEMENT ───────────────────

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<TokenPairResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenPair pair = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(TokenPairResponse.from(pair));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout — revoke all tokens")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }
}
