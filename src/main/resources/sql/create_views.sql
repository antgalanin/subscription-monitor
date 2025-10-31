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
-- Витрина 4: Активность пользователей
-- =============================================================================

CREATE OR REPLACE VIEW analytics.user_activity AS
SELECT
    u.id AS user_id,
    u.username,
    u.email,
    u.created_at AS registration_date,
    COUNT(DISTINCT s.id) AS total_subscriptions_created,
    COUNT(DISTINCT n.id) AS total_notifications,
    COUNT(DISTINCT n.id) FILTER (WHERE n.is_sent = TRUE) AS notifications_sent,
    COUNT(DISTINCT n.id) FILTER (WHERE n.is_sent = FALSE) AS notifications_pending,
    MAX(s.created_at) AS last_subscription_created,
    MAX(n.notification_date) FILTER (WHERE n.is_sent = TRUE) AS last_notification_sent
FROM users u
LEFT JOIN subscriptions s ON u.id = s.user_id
LEFT JOIN notifications n ON u.id = n.user_id
GROUP BY u.id, u.username, u.email, u.created_at
ORDER BY u.created_at DESC;

COMMENT ON VIEW analytics.user_activity IS
'Временная шкала активности пользователей';

-- =============================================================================
-- Витрина 5: Топ подписок
-- =============================================================================

CREATE OR REPLACE VIEW analytics.top_subscriptions AS
SELECT
    s.name AS subscription_name,
    c.name AS category_name,
    COUNT(s.id) AS subscription_count,
    COUNT(s.id) FILTER (WHERE s.is_active = TRUE) AS active_count,
    COUNT(DISTINCT s.user_id) AS unique_users,
    MODE() WITHIN GROUP (ORDER BY p.currency) AS most_common_currency,
    AVG(p.cost) AS avg_cost,
    MIN(p.cost) AS min_cost,
    MAX(p.cost) AS max_cost,
    ROUND(AVG(p.billing_period_days), 0) AS avg_billing_days
FROM subscriptions s
JOIN categories c ON s.category_id = c.id
JOIN payments p ON s.payment_id = p.id
GROUP BY s.name, c.name
HAVING COUNT(s.id) > 0
ORDER BY active_count DESC, subscription_count DESC;

COMMENT ON VIEW analytics.top_subscriptions IS
'Рейтинг подписок по популярности';

-- =============================================================================
-- Витрина 6: Анализ уведомлений
-- =============================================================================

CREATE OR REPLACE VIEW analytics.notification_analysis AS
SELECT
    u.id AS user_id,
    u.username,
    u.notification_days,
    COUNT(n.id) AS total_notifications,
    COUNT(n.id) FILTER (WHERE n.notification_type = 'UPCOMING_PAYMENT') AS upcoming_payment_notifications,
    COUNT(n.id) FILTER (WHERE n.notification_type = 'PAYMENT_SUCCESSFUL') AS payment_successful_notifications,
    COUNT(n.id) FILTER (WHERE n.is_sent = TRUE) AS sent_notifications,
    COUNT(n.id) FILTER (WHERE n.is_sent = FALSE) AS pending_notifications,
    ROUND(
        COUNT(n.id) FILTER (WHERE n.is_sent = TRUE)::NUMERIC /
        NULLIF(COUNT(n.id), 0) * 100, 2
    ) AS sent_rate_percent
FROM users u
LEFT JOIN notifications n ON u.id = n.user_id
GROUP BY u.id, u.username, u.notification_days
ORDER BY total_notifications DESC;

COMMENT ON VIEW analytics.notification_analysis IS
'Анализ уведомлений по пользователям';

-- =============================================================================
-- Витрина 7: Срочные платежи (ближайшие 7 дней)
-- =============================================================================

CREATE OR REPLACE VIEW analytics.urgent_payments AS
SELECT
    u.id AS user_id,
    u.username,
    u.email,
    COUNT(DISTINCT s.id) AS total_urgent_subscriptions,
    COUNT(DISTINCT s.id) FILTER (WHERE p.next_billing_date < CURRENT_DATE) AS overdue_count,
    COUNT(DISTINCT s.id) FILTER (WHERE p.next_billing_date = CURRENT_DATE) AS today_count,
    COUNT(DISTINCT s.id) FILTER (WHERE p.next_billing_date = CURRENT_DATE + INTERVAL '1 day') AS tomorrow_count,
    COUNT(DISTINCT s.id) FILTER (WHERE p.next_billing_date <= CURRENT_DATE + INTERVAL '7 days'
                                   AND p.next_billing_date > CURRENT_DATE) AS this_week_count,
    COALESCE(SUM(p.cost) FILTER (WHERE p.next_billing_date <= CURRENT_DATE + INTERVAL '7 days'
                                   AND p.currency = 'RUB'), 0) AS total_urgent_rub,
    COALESCE(SUM(p.cost) FILTER (WHERE p.next_billing_date <= CURRENT_DATE + INTERVAL '7 days'
                                   AND p.currency = 'USD'), 0) AS total_urgent_usd,
    COALESCE(SUM(p.cost) FILTER (WHERE p.next_billing_date <= CURRENT_DATE + INTERVAL '7 days'
                                   AND p.currency = 'EUR'), 0) AS total_urgent_eur
FROM users u
LEFT JOIN subscriptions s ON u.id = s.user_id AND s.is_active = TRUE
LEFT JOIN payments p ON s.payment_id = p.id
WHERE p.next_billing_date <= CURRENT_DATE + INTERVAL '7 days'
GROUP BY u.id, u.username, u.email
HAVING COUNT(DISTINCT s.id) > 0
ORDER BY overdue_count DESC, today_count DESC;

COMMENT ON VIEW analytics.urgent_payments IS
'Срочные платежи пользователей (ближайшие 7 дней) с детальной разбивкой';

-- =============================================================================
-- Витрина 8: Динамика расходов по месяцам
-- =============================================================================

CREATE OR REPLACE VIEW analytics.spending_trends AS
SELECT
    u.id AS user_id,
    u.username,
    DATE_TRUNC('month', p.next_billing_date)::DATE AS month,
    COUNT(DISTINCT s.id) AS active_subscriptions,
    COALESCE(SUM(p.cost) FILTER (WHERE p.currency = 'RUB'), 0) AS monthly_cost_rub,
    COALESCE(SUM(p.cost) FILTER (WHERE p.currency = 'USD'), 0) AS monthly_cost_usd,
    COALESCE(SUM(p.cost) FILTER (WHERE p.currency = 'EUR'), 0) AS monthly_cost_eur,
    ROUND(AVG(p.cost), 2) AS avg_subscription_cost
FROM users u
JOIN subscriptions s ON u.id = s.user_id AND s.is_active = TRUE
JOIN payments p ON s.payment_id = p.id
WHERE p.next_billing_date >= CURRENT_DATE
    AND p.next_billing_date < CURRENT_DATE + INTERVAL '6 months'
GROUP BY u.id, u.username, DATE_TRUNC('month', p.next_billing_date)
ORDER BY u.username, month;

COMMENT ON VIEW analytics.spending_trends IS
'Прогноз расходов на ближайшие 6 месяцев по пользователям';

-- =============================================================================
-- Витрина 9: Статистика категорий по пользователям
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

-- =============================================================================
-- Витрина 10: Самые дорогие подписки
-- =============================================================================

CREATE OR REPLACE VIEW analytics.expensive_subscriptions AS
SELECT
    u.id AS user_id,
    u.username,
    s.id AS subscription_id,
    s.name AS subscription_name,
    c.name AS category_name,
    p.cost,
    p.currency,
    p.billing_period_days,
    CASE p.billing_period_days
        WHEN 1 THEN p.cost * 30
        WHEN 7 THEN p.cost * 4.3
        WHEN 30 THEN p.cost
        WHEN 90 THEN p.cost / 3
        WHEN 365 THEN p.cost / 12
        ELSE p.cost * 30 / NULLIF(p.billing_period_days, 0)
    END AS monthly_cost_normalized,
    p.next_billing_date
FROM subscriptions s
JOIN users u ON s.user_id = u.id
JOIN payments p ON s.payment_id = p.id
JOIN categories c ON s.category_id = c.id
WHERE s.is_active = TRUE
ORDER BY monthly_cost_normalized DESC, p.cost DESC;

COMMENT ON VIEW analytics.expensive_subscriptions IS
'Самые дорогие подписки с нормализацией к месячной стоимости';

-- =============================================================================
-- Материализованная витрина: Месячная статистика
-- =============================================================================

CREATE MATERIALIZED VIEW analytics.monthly_statistics AS
SELECT
    DATE_TRUNC('month', s.created_at)::DATE AS month,
    COUNT(DISTINCT s.id) AS new_subscriptions,
    COUNT(DISTINCT s.user_id) AS active_users,
    COUNT(DISTINCT c.id) AS categories_used,
    COALESCE(SUM(p.cost) FILTER (WHERE p.currency = 'RUB'), 0) AS total_cost_rub,
    COALESCE(SUM(p.cost) FILTER (WHERE p.currency = 'USD'), 0) AS total_cost_usd,
    COALESCE(SUM(p.cost) FILTER (WHERE p.currency = 'EUR'), 0) AS total_cost_eur,
    ROUND(AVG(p.billing_period_days), 0) AS avg_billing_days
FROM subscriptions s
JOIN payments p ON s.payment_id = p.id
JOIN categories c ON s.category_id = c.id
WHERE s.created_at IS NOT NULL
GROUP BY DATE_TRUNC('month', s.created_at)
ORDER BY month DESC;

CREATE UNIQUE INDEX ON analytics.monthly_statistics (month);

COMMENT ON MATERIALIZED VIEW analytics.monthly_statistics IS
'Ежемесячная статистика по подпискам (материализованное представление)';