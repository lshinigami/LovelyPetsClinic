package com.lovelypets.auth.service;

/**
 * Сервис хеширования паролей.
 * Интерфейс — чтобы алгоритм можно было заменить без изменения бизнес-логики.
 */
public interface PasswordHasher {

    /** Возвращает SHA-256 hex-хеш переданной строки. */
    String hash(String rawPassword);

    /** Проверяет совпадение сырого пароля с уже сохранённым хешем. */
    boolean matches(String rawPassword, String hashedPassword);
}
