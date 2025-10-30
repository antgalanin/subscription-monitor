-- =============================================================================
-- Тестовые данные для системы мониторинга подписок
-- =============================================================================

-- =============================================================================
-- Очистка данных
-- =============================================================================

TRUNCATE TABLE notifications CASCADE;
TRUNCATE TABLE subscriptions CASCADE;
TRUNCATE TABLE payments CASCADE;
TRUNCATE TABLE categories CASCADE;
TRUNCATE TABLE users CASCADE;

-- =============================================================================
-- Вставка пользователей
-- =============================================================================

INSERT INTO users (id, username, email, password, role, notification_days, created_at) VALUES
(uuid_generate_v4(), 'admin', 'admin@submonitor.com', '$2a$10$adminHashedPassword123', 'ADMIN', 7, CURRENT_TIMESTAMP - INTERVAL '365 days'),
(uuid_generate_v4(), 'alice', 'alice@example.com', '$2a$10$aliceHashedPassword123', 'USER', 3, CURRENT_TIMESTAMP - INTERVAL '180 days'),
(uuid_generate_v4(), 'bob', 'bob@example.com', '$2a$10$bobHashedPassword123', 'USER', 5, CURRENT_TIMESTAMP - INTERVAL '120 days'),
(uuid_generate_v4(), 'charlie', 'charlie@example.com', '$2a$10$charlieHashedPassword123', 'USER', 3, CURRENT_TIMESTAMP - INTERVAL '90 days'),
(uuid_generate_v4(), 'diana', 'diana@example.com', '$2a$10$dianaHashedPassword123', 'USER', 7, CURRENT_TIMESTAMP - INTERVAL '60 days');

-- =============================================================================
-- Вставка категорий
-- =============================================================================

INSERT INTO categories (id, name, type, created_by_user_id, created_at) VALUES
(uuid_generate_v4(), 'Streaming', 'SYSTEM', NULL, CURRENT_TIMESTAMP - INTERVAL '400 days'),
(uuid_generate_v4(), 'Cloud Storage', 'SYSTEM', NULL, CURRENT_TIMESTAMP - INTERVAL '400 days'),
(uuid_generate_v4(), 'Software', 'SYSTEM', NULL, CURRENT_TIMESTAMP - INTERVAL '400 days'),
(uuid_generate_v4(), 'Media', 'SYSTEM', NULL, CURRENT_TIMESTAMP - INTERVAL '400 days'),
(uuid_generate_v4(), 'Gaming', 'CUSTOM', (SELECT id FROM users WHERE username = 'alice'), CURRENT_TIMESTAMP - INTERVAL '100 days'),
(uuid_generate_v4(), 'Education', 'CUSTOM', (SELECT id FROM users WHERE username = 'bob'), CURRENT_TIMESTAMP - INTERVAL '80 days');

-- =============================================================================
-- Вставка платежей и подписок
-- =============================================================================

DO $$
DECLARE
    alice_id UUID := (SELECT id FROM users WHERE username = 'alice');
    bob_id UUID := (SELECT id FROM users WHERE username = 'bob');
    charlie_id UUID := (SELECT id FROM users WHERE username = 'charlie');
    diana_id UUID := (SELECT id FROM users WHERE username = 'diana');

    streaming_id UUID := (SELECT id FROM categories WHERE name = 'Streaming');
    cloud_id UUID := (SELECT id FROM categories WHERE name = 'Cloud Storage');
    software_id UUID := (SELECT id FROM categories WHERE name = 'Software');
    gaming_id UUID := (SELECT id FROM categories WHERE name = 'Gaming');

    payment1_id UUID;
    payment2_id UUID;
    payment3_id UUID;
    payment4_id UUID;
    payment5_id UUID;
    payment6_id UUID;
    payment7_id UUID;
    payment8_id UUID;
BEGIN
    -- Netflix для Alice (RUB)
    payment1_id := uuid_generate_v4();
    INSERT INTO payments (id, cost, currency, billing_period_days, next_billing_date, created_at) VALUES
    (payment1_id, 999.00, 'RUB', 30, CURRENT_DATE + INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '180 days');

    INSERT INTO subscriptions (id, user_id, category_id, name, payment_id, is_active, created_at) VALUES
    (uuid_generate_v4(), alice_id, streaming_id, 'Netflix Premium', payment1_id, TRUE, CURRENT_TIMESTAMP - INTERVAL '180 days');

    -- Spotify для Alice (RUB)
    payment2_id := uuid_generate_v4();
    INSERT INTO payments (id, cost, currency, billing_period_days, next_billing_date, created_at) VALUES
    (payment2_id, 299.00, 'RUB', 30, CURRENT_DATE + INTERVAL '12 days', CURRENT_TIMESTAMP - INTERVAL '150 days');

    INSERT INTO subscriptions (id, user_id, category_id, name, payment_id, is_active, created_at) VALUES
    (uuid_generate_v4(), alice_id, streaming_id, 'Spotify Individual', payment2_id, TRUE, CURRENT_TIMESTAMP - INTERVAL '150 days');

    -- Google Drive для Alice (USD)
    payment3_id := uuid_generate_v4();
    INSERT INTO payments (id, cost, currency, billing_period_days, next_billing_date, created_at) VALUES
    (payment3_id, 9.99, 'USD', 30, CURRENT_DATE + INTERVAL '8 days', CURRENT_TIMESTAMP - INTERVAL '170 days');

    INSERT INTO subscriptions (id, user_id, category_id, name, payment_id, is_active, created_at) VALUES
    (uuid_generate_v4(), alice_id, cloud_id, 'Google Drive 100GB', payment3_id, TRUE, CURRENT_TIMESTAMP - INTERVAL '170 days');

    -- Apple Music для Bob (RUB)
    payment4_id := uuid_generate_v4();
    INSERT INTO payments (id, cost, currency, billing_period_days, next_billing_date, created_at) VALUES
    (payment4_id, 199.00, 'RUB', 30, CURRENT_DATE + INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '120 days');

    INSERT INTO subscriptions (id, user_id, category_id, name, payment_id, is_active, created_at) VALUES
    (uuid_generate_v4(), bob_id, streaming_id, 'Apple Music', payment4_id, TRUE, CURRENT_TIMESTAMP - INTERVAL '120 days');

    -- Dropbox для Bob (EUR)
    payment5_id := uuid_generate_v4();
    INSERT INTO payments (id, cost, currency, billing_period_days, next_billing_date, created_at) VALUES
    (payment5_id, 9.99, 'EUR', 30, CURRENT_DATE + INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '110 days');

    INSERT INTO subscriptions (id, user_id, category_id, name, payment_id, is_active, created_at) VALUES
    (uuid_generate_v4(), bob_id, cloud_id, 'Dropbox Plus', payment5_id, TRUE, CURRENT_TIMESTAMP - INTERVAL '110 days');

    -- YouTube Premium для Charlie (RUB)
    payment6_id := uuid_generate_v4();
    INSERT INTO payments (id, cost, currency, billing_period_days, next_billing_date, created_at) VALUES
    (payment6_id, 399.00, 'RUB', 30, CURRENT_DATE - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '90 days');

    INSERT INTO subscriptions (id, user_id, category_id, name, payment_id, is_active, created_at) VALUES
    (uuid_generate_v4(), charlie_id, streaming_id, 'YouTube Premium', payment6_id, TRUE, CURRENT_TIMESTAMP - INTERVAL '90 days');

    -- PlayStation Plus для Charlie (годовая, USD)
    payment7_id := uuid_generate_v4();
    INSERT INTO payments (id, cost, currency, billing_period_days, next_billing_date, created_at) VALUES
    (payment7_id, 59.99, 'USD', 365, CURRENT_DATE + INTERVAL '120 days', CURRENT_TIMESTAMP - INTERVAL '80 days');

    INSERT INTO subscriptions (id, user_id, category_id, name, payment_id, is_active, created_at) VALUES
    (uuid_generate_v4(), charlie_id, gaming_id, 'PlayStation Plus', payment7_id, TRUE, CURRENT_TIMESTAMP - INTERVAL '80 days');

    -- Disney+ для Diana (RUB)
    payment8_id := uuid_generate_v4();
    INSERT INTO payments (id, cost, currency, billing_period_days, next_billing_date, created_at) VALUES
    (payment8_id, 799.00, 'RUB', 30, CURRENT_DATE + INTERVAL '25 days', CURRENT_TIMESTAMP - INTERVAL '60 days');

    INSERT INTO subscriptions (id, user_id, category_id, name, payment_id, is_active, created_at) VALUES
    (uuid_generate_v4(), diana_id, streaming_id, 'Disney+', payment8_id, TRUE, CURRENT_TIMESTAMP - INTERVAL '60 days');

    -- Неактивная подписка для Alice
    INSERT INTO payments (id, cost, currency, billing_period_days, next_billing_date, created_at) VALUES
    (uuid_generate_v4(), 1290.00, 'RUB', 30, CURRENT_DATE, CURRENT_TIMESTAMP - INTERVAL '200 days');

    INSERT INTO subscriptions (id, user_id, category_id, name, payment_id, is_active, created_at) VALUES
    (uuid_generate_v4(), alice_id, software_id, 'Adobe Creative Cloud', (SELECT id FROM payments WHERE cost = 1290.00 AND currency = 'RUB'), FALSE, CURRENT_TIMESTAMP - INTERVAL '200 days');
END $$;

-- =============================================================================
-- Обновление материализованного представления
-- =============================================================================

REFRESH MATERIALIZED VIEW analytics.monthly_statistics;

-- =============================================================================
-- Статистика по загруженным данным
-- =============================================================================

SELECT 'users' AS table_name, COUNT(*) AS record_count FROM users
UNION ALL
SELECT 'categories', COUNT(*) FROM categories
UNION ALL
SELECT 'payments', COUNT(*) FROM payments
UNION ALL
SELECT 'subscriptions', COUNT(*) FROM subscriptions
UNION ALL
SELECT 'notifications', COUNT(*) FROM notifications
ORDER BY table_name;

-- =============================================================================
-- Проверка аналитики
-- =============================================================================

SELECT * FROM analytics.user_subscriptions_summary;
SELECT * FROM analytics.upcoming_payments;
