package com.lovelypets.auth.service;

import com.lovelypets.auth.TokenPair;

/**
 * Сервис аутентификации: логин (Client & Staff) и обновление токенов.
 */
public interface AuthService {

    /**
     * Логин клиента.
     *
     * @param email       email
     * @param rawPassword пароль в открытом виде
     * @return пара JWT-токенов
     * @throws com.lovelypets.auth.exception.InvalidCredentialsException если данные неверны
     * @throws com.lovelypets.auth.exception.AccountNotVerifiedException если OTP не подтверждён
     */
    TokenPair loginClient(String email, String rawPassword);

    /**
     * Логин сотрудника (Veterinarian / Manager).
     * Пароль для стаффа хранится в отдельном механизме — делегируем в StaffCredentials
     * (или временно используем тот же подход, что и для клиентов).
     *
     * @param email       email сотрудника
     * @param rawPassword пароль
     * @return пара JWT-токенов
     */
    TokenPair loginStaff(String email, String rawPassword);

    /**
     * Обновление пары токенов по refresh-токену.
     *
     * @param refreshTokenValue значение refresh-токена
     * @return новая пара токенов
     * @throws com.lovelypets.auth.exception.InvalidTokenException если токен недействителен
     */
    TokenPair refresh(String refreshTokenValue);

    /**
     * Выход: отзывает все токены пользователя.
     *
     * @param accessTokenValue значение текущего access-токена
     */
    void logout(String accessTokenValue);
}
