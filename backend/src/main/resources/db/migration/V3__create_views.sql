-- =============================================================================
-- Аналитические витрины данных
-- =============================================================================

-- =============================================================================
-- Витрина 1: Сводка по подпискам пользователей
-- =============================================================================

CREATE OR REPLACE VIEW analytics.user_subscriptions_summary AS
SELECT
    u.id AS user_id,
    u.username,
    u.email,
    u.role,
    COUNT(s.id) AS total_subscriptions,
    COUNT(s.id) FILTER (WHERE s.is_active = TRUE) AS active_subscriptions,
    COUNT(s.id) FILTER (WHERE s.is_active = FALSE) AS inactive_subscriptions,
    COALESCE(SUM(
        CASE p.billing_period_days
            WHEN 1 THEN p.cost * 30
            WHEN 7 THEN p.cost * 4.3
            WHEN 30 THEN p.cost
            WHEN 90 THEN p.cost / 3
            WHEN 365 THEN p.cost / 12
            ELSE p.cost * 30 / NULLIF(p.billing_period_days, 0)
        END
    ) FILTER (WHERE s.is_active = TRUE AND p.currency = 'RUB'), 0) AS total_cost_rub,
    COALESCE(SUM(
        CASE p.billing_period_days
            WHEN 1 THEN p.cost * 30
            WHEN 7 THEN p.cost * 4.3
            WHEN 30 THEN p.cost
            WHEN 90 THEN p.cost / 3
            WHEN 365 THEN p.cost / 12
            ELSE p.cost * 30 / NULLIF(p.billing_period_days, 0)
        END
    ) FILTER (WHERE s.is_active = TRUE AND p.currency = 'USD'), 0) AS total_cost_usd,
    COALESCE(SUM(
        CASE p.billing_period_days
            WHEN 1 THEN p.cost * 30
            WHEN 7 THEN p.cost * 4.3
            WHEN 30 THEN p.cost
            WHEN 90 THEN p.cost / 3
            WHEN 365 THEN p.cost / 12
            ELSE p.cost * 30 / NULLIF(p.billing_period_days, 0)
        END
    ) FILTER (WHERE s.is_active = TRUE AND p.currency = 'EUR'), 0) AS total_cost_eur,
    ROUND(AVG(p.billing_period_days) FILTER (WHERE s.is_active = TRUE), 0) AS avg_billing_period_days
FROM users u
LEFT JOIN subscriptions s ON u.id = s.user_id
LEFT JOIN payments p ON s.payment_id = p.id
GROUP BY u.id, u.username, u.email, u.role
ORDER BY active_subscriptions DESC;

COMMENT ON VIEW analytics.user_subscriptions_summary IS
'Сводка подписок по пользователям с разбивкой по валютам';

-- =============================================================================
-- Витрина 2: Предстоящие платежи
-- =============================================================================

CREATE OR REPLACE VIEW analytics.upcoming_payments AS
SELECT
    u.id AS user_id,
    u.username,
    u.email,
    s.id AS subscription_id,
    s.name AS subscription_name,
    c.name AS category_name,
    p.cost,
    p.currency,
    p.next_billing_date,
    p.billing_period_days,
    CASE
        WHEN p.next_billing_date < CURRENT_DATE THEN 'Overdue'
        WHEN p.next_billing_date = CURRENT_DATE THEN 'Today'
        WHEN p.next_billing_date = CURRENT_DATE + INTERVAL '1 day' THEN 'Tomorrow'
        WHEN p.next_billing_date = CURRENT_DATE + INTERVAL '2 days' THEN 'In 2 Days'
        WHEN p.next_billing_date = CURRENT_DATE + INTERVAL '3 days' THEN 'In 3 Days'
        WHEN p.next_billing_date <= CURRENT_DATE + INTERVAL '7 days' THEN 'This Week'
        WHEN p.next_billing_date <= CURRENT_DATE + INTERVAL '30 days' THEN 'This Month'
        ELSE 'Later'
    END AS payment_urgency,
    (CURRENT_DATE - p.next_billing_date) AS days_overdue,
    (p.next_billing_date - CURRENT_DATE) AS days_until_payment
FROM subscriptions s
JOIN users u ON s.user_id = u.id
JOIN payments p ON s.payment_id = p.id
JOIN categories c ON s.category_id = c.id
WHERE s.is_active = TRUE
ORDER BY p.next_billing_date ASC;

COMMENT ON VIEW analytics.upcoming_payments IS
'Предстоящие платежи с категоризацией по срочности';

-- =============================================================================
-- Витрина 3: Статистика по категориям
-- =============================================================================

CREATE OR REPLACE VIEW analytics.category_statistics AS
SELECT
    c.id AS category_id,
    c.name AS category_name,
    c.type AS category_type,
    COUNT(s.id) AS total_subscriptions,
    COUNT(s.id) FILTER (WHERE s.is_active = TRUE) AS active_subscriptions,
    COUNT(DISTINCT s.user_id) AS unique_users,
    COALESCE(SUM(p.cost) FILTER (WHERE s.is_active = TRUE AND p.currency = 'RUB'), 0) AS total_cost_rub,
    COALESCE(SUM(p.cost) FILTER (WHERE s.is_active = TRUE AND p.currency = 'USD'), 0) AS total_cost_usd,
    COALESCE(SUM(p.cost) FILTER (WHERE s.is_active = TRUE AND p.currency = 'EUR'), 0) AS total_cost_eur,
    COALESCE(AVG(p.cost) FILTER (WHERE s.is_active = TRUE), 0) AS avg_cost,
    ROUND(AVG(p.billing_period_days) FILTER (WHERE s.is_active = TRUE), 0) AS avg_billing_days
FROM categories c
LEFT JOIN subscriptions s ON c.id = s.category_id
LEFT JOIN payments p ON s.payment_id = p.id
GROUP BY c.id, c.name, c.type
ORDER BY active_subscriptions DESC;

COMMENT ON VIEW analytics.category_statistics IS
'Статистика по категориям с разбивкой по валютам';

-- =============================================================================
-- Витрина 4: Статистика категорий по пользователям
-- =============================================================================

CREATE OR REPLACE VIEW analytics.user_category_statistics AS
SELECT
    u.id AS user_id,
    u.username,
    c.id AS category_id,
    c.name AS category_name,
    c.type AS category_type,
    COUNT(s.id) AS total_subscriptions,
    COUNT(s.id) FILTER (WHERE s.is_active = TRUE) AS active_subscriptions,
    1 AS unique_users,
    COALESCE(SUM(p.cost) FILTER (WHERE s.is_active = TRUE AND p.currency = 'RUB'), 0) AS total_cost_rub,
    COALESCE(SUM(p.cost) FILTER (WHERE s.is_active = TRUE AND p.currency = 'USD'), 0) AS total_cost_usd,
    COALESCE(SUM(p.cost) FILTER (WHERE s.is_active = TRUE AND p.currency = 'EUR'), 0) AS total_cost_eur,
    COALESCE(AVG(p.cost) FILTER (WHERE s.is_active = TRUE), 0) AS avg_cost,
    ROUND(AVG(p.billing_period_days) FILTER (WHERE s.is_active = TRUE), 0) AS avg_billing_days
FROM users u
         CROSS JOIN categories c
         LEFT JOIN subscriptions s ON c.id = s.category_id AND s.user_id = u.id
         LEFT JOIN payments p ON s.payment_id = p.id
GROUP BY u.id, u.username, c.id, c.name, c.type
HAVING COUNT(s.id) > 0
ORDER BY u.id, active_subscriptions DESC;

COMMENT ON VIEW analytics.user_category_statistics IS
'Статистика категорий персонализированная для каждого пользователя';

