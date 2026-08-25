-- =============================================================================
-- Индексы для оптимизации производительности
-- =============================================================================

-- =============================================================================
-- Индексы для таблицы users
-- =============================================================================

CREATE INDEX idx_users_username ON users(username);
COMMENT ON INDEX idx_users_username IS 'Ускоряет поиск по username (авторизация)';

CREATE INDEX idx_users_email ON users(email);
COMMENT ON INDEX idx_users_email IS 'Ускоряет поиск по email';

CREATE INDEX idx_users_role ON users(role);
COMMENT ON INDEX idx_users_role IS 'Ускоряет фильтрацию по роли';

CREATE INDEX idx_users_created_at ON users(created_at);
COMMENT ON INDEX idx_users_created_at IS 'Ускоряет сортировку по дате регистрации';

-- =============================================================================
-- Индексы для таблицы categories
-- =============================================================================

CREATE INDEX idx_categories_name ON categories(name);
COMMENT ON INDEX idx_categories_name IS 'Ускоряет поиск категории по имени';

CREATE INDEX idx_categories_type ON categories(type);
COMMENT ON INDEX idx_categories_type IS 'Ускоряет фильтрацию по типу категории';

CREATE INDEX idx_categories_created_by_user ON categories(created_by_user_id)
WHERE created_by_user_id IS NOT NULL;
COMMENT ON INDEX idx_categories_created_by_user IS 'Partial индекс для пользовательских категорий';

-- =============================================================================
-- Индексы для таблицы payments
-- =============================================================================

CREATE INDEX idx_payments_currency ON payments(currency);
COMMENT ON INDEX idx_payments_currency IS 'Ускоряет фильтрацию по валюте';

CREATE INDEX idx_payments_next_billing_date ON payments(next_billing_date);
COMMENT ON INDEX idx_payments_next_billing_date IS 'Ускоряет поиск платежей по дате';

CREATE INDEX idx_payments_billing_period ON payments(billing_period_days);
COMMENT ON INDEX idx_payments_billing_period IS 'Ускоряет аналитику по периодам оплаты';

CREATE INDEX idx_payments_cost ON payments(cost);
COMMENT ON INDEX idx_payments_cost IS 'Ускоряет сортировку по стоимости';

-- Partial индекс с CURRENT_DATE не может быть создан (функция не IMMUTABLE)
-- Вместо этого используем обычный индекс по next_billing_date (уже создан выше)

-- =============================================================================
-- Индексы для таблицы subscriptions
-- =============================================================================

CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
COMMENT ON INDEX idx_subscriptions_user_id IS 'Ускоряет поиск подписок пользователя';

CREATE INDEX idx_subscriptions_category_id ON subscriptions(category_id);
COMMENT ON INDEX idx_subscriptions_category_id IS 'Ускоряет фильтрацию по категории';

CREATE INDEX idx_subscriptions_payment_id ON subscriptions(payment_id);
COMMENT ON INDEX idx_subscriptions_payment_id IS 'Ускоряет поиск по payment_id (уникальный FK)';

CREATE INDEX idx_subscriptions_is_active ON subscriptions(is_active);
COMMENT ON INDEX idx_subscriptions_is_active IS 'Ускоряет фильтрацию активных подписок';

CREATE INDEX idx_subscriptions_name ON subscriptions(name);
COMMENT ON INDEX idx_subscriptions_name IS 'Ускоряет поиск и группировку по названию';

CREATE INDEX idx_subscriptions_created_at ON subscriptions(created_at);
COMMENT ON INDEX idx_subscriptions_created_at IS 'Ускоряет сортировку по дате создания';

CREATE INDEX idx_subscriptions_user_active ON subscriptions(user_id, is_active)
WHERE is_active = TRUE;
COMMENT ON INDEX idx_subscriptions_user_active IS 'Partial индекс для активных подписок пользователя';

CREATE INDEX idx_subscriptions_category_active ON subscriptions(category_id, is_active)
WHERE is_active = TRUE;
COMMENT ON INDEX idx_subscriptions_category_active IS 'Partial индекс для активных подписок в категории';

-- =============================================================================
-- Индексы для таблицы notifications
-- =============================================================================

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
COMMENT ON INDEX idx_notifications_user_id IS 'Ускоряет поиск уведомлений пользователя';

CREATE INDEX idx_notifications_subscription_id ON notifications(subscription_id);
COMMENT ON INDEX idx_notifications_subscription_id IS 'Ускоряет поиск уведомлений по подписке';

CREATE INDEX idx_notifications_type ON notifications(notification_type);
COMMENT ON INDEX idx_notifications_type IS 'Ускоряет фильтрацию по типу уведомления';

CREATE INDEX idx_notifications_is_sent ON notifications(is_sent);
COMMENT ON INDEX idx_notifications_is_sent IS 'Ускоряет фильтрацию отправленных/неотправленных';

CREATE INDEX idx_notifications_date ON notifications(notification_date);
COMMENT ON INDEX idx_notifications_date IS 'Ускоряет сортировку по дате уведомления';

CREATE INDEX idx_notifications_pending ON notifications(user_id, is_sent, notification_date)
WHERE is_sent = FALSE;
COMMENT ON INDEX idx_notifications_pending IS 'Partial индекс для неотправленных уведомлений';

CREATE INDEX idx_notifications_created_at ON notifications(created_at);
COMMENT ON INDEX idx_notifications_created_at IS 'Ускоряет сортировку по дате создания';

-- =============================================================================
-- BRIN индексы для больших таблиц
-- =============================================================================

CREATE INDEX idx_subscriptions_created_at_brin ON subscriptions USING BRIN (created_at);
COMMENT ON INDEX idx_subscriptions_created_at_brin IS
'BRIN индекс для эффективного диапазонного поиска по дате создания';

CREATE INDEX idx_notifications_created_at_brin ON notifications USING BRIN (created_at);
COMMENT ON INDEX idx_notifications_created_at_brin IS
'BRIN индекс для эффективного диапазонного поиска по дате';