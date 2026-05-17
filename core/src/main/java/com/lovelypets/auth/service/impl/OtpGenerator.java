package com.lovelypets.auth.service.impl;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Генерирует криптографически безопасный 6-значный OTP-код.
 */
@Component
public class OtpGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_LENGTH = 6;

    public String generate() {
        int bound = (int) Math.pow(10, OTP_LENGTH);      // 1_000_000
        int code  = RANDOM.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", code);
    }
}
