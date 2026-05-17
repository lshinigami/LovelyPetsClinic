package com.lovelypets.auth.service;

/**
 * Сервис регистрации клиента (двухшаговый процесс с OTP).
 */
public interface RegistrationService {

    /**
     * Шаг 1: сохраняет email + password-hash, генерирует OTP,
     * сохраняет его в БД и публикует {@code OtpRequestedEvent} в Kafka.
     *
     * @param email       email клиента
     * @param rawPassword пароль в открытом виде (будет хеширован SHA-256)
     * @throws com.lovelypets.auth.exception.EmailAlreadyRegisteredException если email занят
     */
    void initiateRegistration(String email, String rawPassword);

    /**
     * Шаг 2: проверяет OTP-код.
     * При успехе помечает клиента как verified и удаляет OTP из БД.
     *
     * @param email   email клиента
     * @param otpCode введённый код
     * @throws com.lovelypets.auth.exception.InvalidOtpException если код неверен или истёк
     */
    void verifyOtp(String email, String otpCode);
}
