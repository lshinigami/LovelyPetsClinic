package com.lovelypets.auth.service.impl;

import com.lovelypets.auth.exception.AccountNotVerifiedException;
import com.lovelypets.auth.exception.EmailAlreadyRegisteredException;
import com.lovelypets.auth.exception.InvalidOtpException;
import com.lovelypets.auth.port.OtpEventPublisher;
import com.lovelypets.auth.repository.ClientCredentialsRepository;
import com.lovelypets.auth.repository.OtpEntryRepository;
import com.lovelypets.auth.service.PasswordHasher;
import com.lovelypets.auth.service.RegistrationService;
import com.lovelypets.entities.ClientCredentials;
import com.lovelypets.entities.OtpEntry;
import com.lovelypets.events.OtpRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Реализация двухшагового флоу регистрации клиента:
 * <ol>
 *   <li>Сохранить email + password hash → сгенерировать OTP → отправить Kafka-событие.</li>
 *   <li>Клиент вводит OTP → верифицировать → пометить аккаунт как verified.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final ClientCredentialsRepository credentialsRepository;
    private final OtpEntryRepository           otpEntryRepository;
    private final PasswordHasher               passwordHasher;
    private final OtpGenerator                 otpGenerator;
    private final OtpEventPublisher            otpEventPublisher;

    /** Сколько минут действует OTP-код. */
    @Value("${auth.otp.expiration-minutes:10}")
    private int otpExpirationMinutes;

    // ─────────────── Шаг 1 ───────────────

    @Override
    @Transactional
    public void initiateRegistration(String email, String rawPassword) {
        if (credentialsRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        // Сохраняем credentials (unverified)
        String passwordHash = passwordHasher.hash(rawPassword);
        ClientCredentials credentials = ClientCredentials.builder()
                .email(email)
                .passwordHash(passwordHash)
                .verified(false)
                .build();
        credentialsRepository.save(credentials);

        // Генерируем OTP и сохраняем в БД
        String otp = otpGenerator.generate();
        saveOrReplaceOtp(email, otp);

        // Публикуем событие в Kafka — consumer отправит письмо
        otpEventPublisher.publish(new OtpRequestedEvent(email, otp));
        log.info("Registration initiated for email={}, OTP published to Kafka", email);
    }

    // ─────────────── Шаг 2 ───────────────

    @Override
    @Transactional
    public void verifyOtp(String email, String otpCode) {
        OtpEntry entry = otpEntryRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOtpException("OTP not found for email: " + email));

        if (entry.isExpired()) {
            otpEntryRepository.deleteByEmail(email);
            throw new InvalidOtpException("OTP has expired. Please register again.");
        }

        if (!entry.getCode().equals(otpCode)) {
            throw new InvalidOtpException("Invalid OTP code.");
        }

        // Всё ОК — помечаем аккаунт как верифицированный
        ClientCredentials credentials = credentialsRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOtpException("Credentials not found for email: " + email));
        credentials.setVerified(true);
        credentialsRepository.save(credentials);

        // Обновляем использованный OTP
        entry.setUsed(true);
        otpEntryRepository.save(entry);
        log.info("OTP verified successfully for email={}", email);
    }

    // ─────────────── helpers ───────────────

    /**
     * Если OTP уже существует (например, клиент нажал «отправить повторно») —
     * заменяем старый новым.
     */
    private void saveOrReplaceOtp(String email, String otp) {
        otpEntryRepository.findByEmail(email)
                .ifPresent(existing -> otpEntryRepository.deleteByEmail(email));

        OtpEntry entry = OtpEntry.builder()
                .email(email)
                .code(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .build();
        otpEntryRepository.save(entry);
    }
}
