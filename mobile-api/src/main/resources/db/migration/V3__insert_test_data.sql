-- ============================================================
--  Lovely Pets Clinic — Seed / Test Data
--  Порядок вставки соблюдает FK-зависимости
-- ============================================================

-- -------------------------------------------------------
--  1. CLIENTS
-- -------------------------------------------------------
INSERT INTO clients (first_name, last_name, email, phone)
VALUES ('Alice', 'Johnson', 'alice.johnson@example.com', '+1-555-0101'),
       ('Bob', 'Smith', 'bob.smith@example.com', '+1-555-0102'),
       ('Carol', 'Williams', 'carol.williams@example.com', '+1-555-0103'),
       ('David', 'Brown', 'david.brown@example.com', '+1-555-0104'),
       ('Eva', 'Davis', 'eva.davis@example.com', '+1-555-0105');

-- -------------------------------------------------------
--  2. FILES_METADATA  (паспорта питомцев + рецепты)
-- -------------------------------------------------------
INSERT INTO files_metadata (name, extension, file_size_bytes, storage_path, entity_type)
VALUES
    -- паспорта питомцев (id 1-5)
    ('passport_fluffy', 'pdf', 102400, '/storage/pets/passport_fluffy.pdf', 'PET_PASSPORT'),
    ('passport_rex', 'pdf', 98304, '/storage/pets/passport_rex.pdf', 'PET_PASSPORT'),
    ('passport_whiskers', 'pdf', 110592, '/storage/pets/passport_whiskers.pdf', 'PET_PASSPORT'),
    ('passport_luna', 'pdf', 107520, '/storage/pets/passport_luna.pdf', 'PET_PASSPORT'),
    ('passport_max', 'pdf', 95232, '/storage/pets/passport_max.pdf', 'PET_PASSPORT'),
    -- файлы рецептов (id 6-9)
    ('prescription_001', 'pdf', 51200, '/storage/prescriptions/rx_001.pdf', 'PRESCRIPTION'),
    ('prescription_002', 'pdf', 48128, '/storage/prescriptions/rx_002.pdf', 'PRESCRIPTION'),
    ('prescription_003', 'pdf', 53248, '/storage/prescriptions/rx_003.pdf', 'PRESCRIPTION'),
    ('prescription_004', 'pdf', 49152, '/storage/prescriptions/rx_004.pdf', 'PRESCRIPTION');

-- -------------------------------------------------------
--  3. PETS
-- -------------------------------------------------------
INSERT INTO pets (client_id, passport_id, name, species, breed, birth_date, gender)
VALUES (1, 1, 'Fluffy', 'Cat', 'Persian', '2019-03-15', 'FEMALE'),
       (2, 2, 'Rex', 'Dog', 'German Shepherd', '2018-07-22', 'MALE'),
       (3, 3, 'Whiskers', 'Cat', 'Maine Coon', '2020-11-05', 'MALE'),
       (4, 4, 'Luna', 'Dog', 'Labrador', '2021-02-18', 'FEMALE'),
       (5, 5, 'Max', 'Dog', 'Bulldog', '2017-09-30', 'MALE');

-- -------------------------------------------------------
--  4. STAFF  (2 вет-врача + 1 менеджер)
-- -------------------------------------------------------
INSERT INTO staff (first_name, last_name, email, phone, role, hire_date, is_active)
VALUES ('Dr. Emma', 'Carter', 'emma.carter@lovelypets.com', '+1-555-0201', 'VETERINARIAN', '2018-05-01', TRUE),
       ('Dr. James', 'Mitchell', 'james.mitchell@lovelypets.com', '+1-555-0202', 'VETERINARIAN', '2020-08-15', TRUE),
       ('Sarah', 'Lee', 'sarah.lee@lovelypets.com', '+1-555-0203', 'MANAGER', '2019-01-10', TRUE);

-- -------------------------------------------------------
--  5. VETERINARIANS
-- -------------------------------------------------------
INSERT INTO veterinarians (staff_id, specialization)
VALUES (1, 'Feline Medicine'),
       (2, 'Canine Surgery');

-- -------------------------------------------------------
--  6. MANAGERS
-- -------------------------------------------------------
INSERT INTO managers (staff_id)
VALUES (3);

-- -------------------------------------------------------
--  7. MEDICINES
-- -------------------------------------------------------
INSERT INTO medicines (name, description, unit, quantity_in_stock, minimum_threshold)
VALUES ('Amoxicillin 250mg', 'Broad-spectrum antibiotic', 'tablet', 500, 50),
       ('Metacam 1.5mg/ml', 'NSAID — pain and inflammation', 'ml', 300, 30),
       ('Frontline Plus', 'Flea and tick preventive treatment', 'pipette', 120, 20),
       ('Dexamethasone 2mg', 'Corticosteroid anti-inflammatory', 'tablet', 200, 25),
       ('Vitamin B complex', 'General nutritional supplement', 'ml', 150, 15);

-- -------------------------------------------------------
--  8. APPOINTMENTS  (все возможные статусы)
-- -------------------------------------------------------
INSERT INTO appointments
(pet_id, client_id, vet_id, manager_id, appointment_date, reason, status)
VALUES
    -- SCHEDULED — запись в будущем
    (1, 1, 1, 3, NOW() + INTERVAL '3 days', 'Annual check-up and vaccination', 'SCHEDULED'),
    (4, 4, 2, 3, NOW() + INTERVAL '7 days', 'Routine dental cleaning', 'SCHEDULED'),

    -- IN_PROGRESS — приём сейчас
    (2, 2, 2, 3, NOW() - INTERVAL '30 minutes', 'Post-surgery wound inspection', 'IN_PROGRESS'),

    -- COMPLETED — завершённые
    (1, 1, 1, 3, NOW() - INTERVAL '30 days', 'Skin allergy treatment', 'COMPLETED'),
    (3, 3, 1, 3, NOW() - INTERVAL '14 days', 'Respiratory infection — follow-up', 'COMPLETED'),
    (2, 2, 2, 3, NOW() - INTERVAL '60 days', 'ACL repair pre-op assessment', 'COMPLETED'),
    (5, 5, 2, 3, NOW() - INTERVAL '7 days', 'Ear infection treatment', 'COMPLETED'),

    -- CANCELLED — отменённые клиентом
    (4, 4, 1, 3, NOW() - INTERVAL '10 days', 'Eye discharge examination', 'CANCELLED'),
    (3, 3, 2, 3, NOW() + INTERVAL '1 day', 'Vaccination booster', 'CANCELLED'),

    -- NO_SHOW — клиент не явился
    (5, 5, 1, 3, NOW() - INTERVAL '5 days', 'Weight management consultation', 'NO_SHOW'),
    (2, 2, 1, 3, NOW() - INTERVAL '21 days', 'Hip dysplasia screening', 'NO_SHOW');

-- -------------------------------------------------------
--  9. PRESCRIPTIONS  (только для COMPLETED appointments)
-- -------------------------------------------------------
-- Appointment id 4 (Fluffy — skin allergy)
INSERT INTO prescriptions (appointment_id, vet_id, file_id, diagnosis, instructions)
VALUES (4, 1, 6,
        'Atopic dermatitis with secondary bacterial infection',
        'Administer Amoxicillin 250 mg twice daily for 10 days. Apply Metacam 0.5 ml once daily for 5 days. Recheck in 2 weeks.');

-- Appointment id 5 (Whiskers — respiratory)
INSERT INTO prescriptions (appointment_id, vet_id, file_id, diagnosis, instructions)
VALUES (5, 1, 7,
        'Upper respiratory tract infection (feline herpesvirus)',
        'Administer Dexamethasone 2 mg once daily for 7 days. Supportive care: warm environment, hydration. Follow-up if no improvement in 5 days.');

-- Appointment id 6 (Rex — pre-op)
INSERT INTO prescriptions (appointment_id, vet_id, file_id, diagnosis, instructions)
VALUES (6, 2, 8,
        'Cranial cruciate ligament partial tear — surgery approved',
        'Pre-op: NPO after midnight. Post-op: Metacam 1.5 ml once daily for 14 days, restricted activity for 8 weeks.');

-- Appointment id 7 (Max — ear infection)
INSERT INTO prescriptions (appointment_id, vet_id, file_id, diagnosis, instructions)
VALUES (7, 2, 9,
        'Otitis externa — bacterial',
        'Clean ears daily with provided solution. Apply Dexamethasone drops twice daily for 10 days. Return if no improvement in 1 week.');

-- -------------------------------------------------------
--  10. APPOINTMENT_MEDICINE  (для завершённых приёмов)
-- -------------------------------------------------------
-- Appointment 4 (skin allergy — Fluffy)
INSERT INTO appointment_medicine (appointment_id, medicine_id, quantity_used)
VALUES (4, 1, 20), -- Amoxicillin × 20 таблеток
       (4, 2, 10);
-- Metacam 10 ml

-- Appointment 5 (respiratory — Whiskers)
INSERT INTO appointment_medicine (appointment_id, medicine_id, quantity_used)
VALUES (5, 4, 7), -- Dexamethasone × 7 таблеток
       (5, 5, 5);
-- Vitamin B × 5 ml

-- Appointment 6 (pre-op — Rex)
INSERT INTO appointment_medicine (appointment_id, medicine_id, quantity_used)
VALUES (6, 2, 30);
-- Metacam 30 ml (post-op курс)

-- Appointment 7 (ear — Max)
INSERT INTO appointment_medicine (appointment_id, medicine_id, quantity_used)
VALUES (7, 4, 10);
-- Dexamethasone × 10 таблеток

-- -------------------------------------------------------
--  11. REVIEWS  (только для COMPLETED appointments)
-- -------------------------------------------------------
INSERT INTO reviews (client_id, vet_id, appointment_id, rating, comment)
VALUES (1, 1, 4, 5, 'Dr. Carter was wonderful with Fluffy! The treatment worked perfectly.'),
       (3, 1, 5, 4, 'Very thorough examination. Whiskers recovered quickly. Slightly long wait time.'),
       (2, 2, 6, 5, 'Dr. Mitchell explained everything clearly. We feel confident about the surgery.'),
       (5, 2, 7, 3, 'Treatment helped, but we had to wait 45 minutes past our scheduled time.');

-- -------------------------------------------------------
--  12. AUTH TABLES
-- -------------------------------------------------------

-- CLIENT CREDENTIALS  (пароль: "Test1234!" → SHA-256)
INSERT INTO client_credentials (email, password_hash, verified)
VALUES ('alice.johnson@example.com', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', TRUE),
       ('bob.smith@example.com', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', TRUE),
       ('carol.williams@example.com', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', TRUE),
       ('david.brown@example.com', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3',
        FALSE), -- не подтверждён
       ('eva.davis@example.com', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', TRUE);

-- STAFF CREDENTIALS
INSERT INTO staff_credentials (email, password_hash)
VALUES ('emma.carter@lovelypets.com', 'b3a8e0e1f9ab1bfe3a36f231f676f78bb28a2028aa4a90c6c9d5c5d4c8f2e5e0'),
       ('james.mitchell@lovelypets.com', 'b3a8e0e1f9ab1bfe3a36f231f676f78bb28a2028aa4a90c6c9d5c5d4c8f2e5e0'),
       ('sarah.lee@lovelypets.com', 'b3a8e0e1f9ab1bfe3a36f231f676f78bb28a2028aa4a90c6c9d5c5d4c8f2e5e0');
