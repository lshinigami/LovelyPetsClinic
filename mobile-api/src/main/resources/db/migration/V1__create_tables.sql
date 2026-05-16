-- ============================================================
--  Lovely Pets Clinic — Database Schema (PostgreSQL)
-- ============================================================

-- -------------------------------------------------------
--  ENUM TYPES
-- -------------------------------------------------------
CREATE TYPE gender_type AS ENUM ('MALE', 'FEMALE', 'UNKNOWN');
CREATE TYPE staff_role AS ENUM ('VETERINARIAN', 'MANAGER');
CREATE TYPE appointment_status AS ENUM ('SCHEDULED', 'COMPLETED', 'CANCELLED');
CREATE TYPE entity_type AS ENUM ('PET_PASSPORT', 'PRESCRIPTION');

-- -------------------------------------------------------
-- 1. CLIENTS
-- -------------------------------------------------------
CREATE TABLE clients
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    first_name VARCHAR(255)        NOT NULL,
    last_name  VARCHAR(255)        NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    phone      VARCHAR(255)        NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------
-- 2. FILES_METADATA  (before Pets & Prescriptions — они ссылаются на неё)
-- -------------------------------------------------------
CREATE TABLE files_metadata
(
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name            VARCHAR(255) NOT NULL,
    extension       VARCHAR(50),
    file_size_bytes BIGINT,
    storage_path    VARCHAR(500) NOT NULL,
    entity_type     entity_type  NOT NULL,
    uploaded_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------
-- 3. PETS
-- -------------------------------------------------------
CREATE TABLE pets
(
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    client_id   BIGINT       NOT NULL REFERENCES clients (id),
    passport_id BIGINT       NOT NULL REFERENCES files_metadata (id),
    name        VARCHAR(100) NOT NULL,
    species     VARCHAR(50)  NOT NULL,
    breed       VARCHAR(100),
    birth_date  DATE,
    gender      gender_type DEFAULT 'UNKNOWN',
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------
-- 4. STAFF
-- -------------------------------------------------------
CREATE TABLE staff
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    first_name VARCHAR(255)        NOT NULL,
    last_name  VARCHAR(255)        NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    phone      VARCHAR(255),
    role       staff_role          NOT NULL,
    hire_date  DATE,
    is_active  BOOLEAN DEFAULT TRUE
);

-- -------------------------------------------------------
-- 5. VETERINARIANS  (extends Staff via JOINED inheritance)
-- -------------------------------------------------------
CREATE TABLE veterinarians
(
    staff_id       BIGINT PRIMARY KEY REFERENCES staff (id),
    specialization VARCHAR(100)
);

-- -------------------------------------------------------
-- 6. MANAGERS  (extends Staff via JOINED inheritance)
-- -------------------------------------------------------
CREATE TABLE managers
(
    staff_id BIGINT PRIMARY KEY REFERENCES staff (id)
);

-- -------------------------------------------------------
-- 7. APPOINTMENTS
-- -------------------------------------------------------
CREATE TABLE appointments
(
    id               BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    pet_id           BIGINT    NOT NULL REFERENCES pets (id),
    client_id        BIGINT    NOT NULL REFERENCES clients (id),
    vet_id           BIGINT    NOT NULL REFERENCES veterinarians (staff_id),
    manager_id       BIGINT REFERENCES managers (staff_id),
    appointment_date TIMESTAMP NOT NULL,
    reason           VARCHAR(255),
    status           appointment_status DEFAULT 'SCHEDULED',
    created_at       TIMESTAMP          DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------
-- 8. MEDICINES
-- -------------------------------------------------------
CREATE TABLE medicines
(
    id                BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name              VARCHAR(100) NOT NULL,
    description       TEXT,
    unit              VARCHAR(20),
    quantity_in_stock BIGINT    DEFAULT 0,
    minimum_threshold BIGINT    DEFAULT 10,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Триггер: обновляет updated_at при каждом UPDATE (аналог ON UPDATE CURRENT_TIMESTAMP)
CREATE
OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at
= CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER medicines_updated_at
    BEFORE UPDATE
    ON medicines
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- -------------------------------------------------------
-- 9. PRESCRIPTIONS
-- -------------------------------------------------------
CREATE TABLE prescriptions
(
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    appointment_id BIGINT NOT NULL UNIQUE REFERENCES appointments (id),
    vet_id         BIGINT NOT NULL REFERENCES veterinarians (staff_id),
    file_id        BIGINT NOT NULL REFERENCES files_metadata (id),
    diagnosis      TEXT   NOT NULL,
    instructions   TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------
-- 10. APPOINTMENT_MEDICINE
-- -------------------------------------------------------
CREATE TABLE appointment_medicine
(
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    appointment_id BIGINT NOT NULL REFERENCES appointments (id),
    medicine_id    BIGINT NOT NULL REFERENCES medicines (id),
    quantity_used  BIGINT DEFAULT 1
);

-- -------------------------------------------------------
-- 11. REVIEWS
-- -------------------------------------------------------
CREATE TABLE reviews
(
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    client_id      BIGINT   NOT NULL REFERENCES clients (id),
    vet_id         BIGINT   NOT NULL REFERENCES veterinarians (staff_id),
    appointment_id BIGINT   NOT NULL UNIQUE REFERENCES appointments (id),
    rating         SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment        TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);