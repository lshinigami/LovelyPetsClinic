-- ============================================================
--  Auth Tables — added in V2
-- ============================================================

-- -------------------------------------------------------
-- 1. CLIENT CREDENTIALS
--    Хранит логин/пароль клиента отдельно от бизнес-данных
-- -------------------------------------------------------
CREATE TABLE client_credentials
(
    id            BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(64)         NOT NULL, -- SHA-256 hex (64 символа)
    verified      BOOLEAN             NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP                    DEFAULT CURRENT_TIMESTAMP
);


-- -------------------------------------------------------
-- 2. CLIENT CREDENTIALS
--    Хранит логин/пароль сотрудников отдельно от бизнес-данных
-- -------------------------------------------------------
CREATE TABLE staff_credentials
(
    id            BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(64)         NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------
-- 3. OTP ENTRIES
--    Временные OTP-коды, удаляются после верификации
-- -------------------------------------------------------
CREATE TABLE otp_entries
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    email      VARCHAR(255) UNIQUE NOT NULL,
    code       VARCHAR(6)          NOT NULL,
    used       BOOLEAN             NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP           NOT NULL,
    created_at TIMESTAMP                    DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------
-- 4. TOKEN ENTRIES
--    Выданные JWT-токены — для revoke-логики
-- -------------------------------------------------------

CREATE TABLE token_entries
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    value      VARCHAR(2048) NOT NULL UNIQUE,
    user_id    BIGINT        NOT NULL,
    user_type  VARCHAR(50)   NOT NULL,
    token_type VARCHAR(50)   NOT NULL,
    issued_at  TIMESTAMP     NOT NULL,
    expires_at TIMESTAMP     NOT NULL,
    revoked    BOOLEAN       NOT NULL DEFAULT FALSE
);

-- Индекс для быстрого поиска активных токенов пользователя
CREATE INDEX idx_token_entries_user ON token_entries (user_id, user_type, revoked);
