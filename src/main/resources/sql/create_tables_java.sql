-- =============================================================================
-- Создание таблиц (JAVA MODE - VARCHAR + CHECK)
-- =============================================================================

-- =============================================================================
-- Таблица users
-- =============================================================================

CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    notification_days INTEGER NOT NULL DEFAULT 3,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_role_check CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT users_notification_days_check CHECK (notification_days >= 0)
);

COMMENT ON TABLE users IS 'Пользователи системы';
COMMENT ON COLUMN users.id IS 'UUID пользователя';
COMMENT ON COLUMN users.username IS 'Имя пользователя (логин)';
COMMENT ON COLUMN users.email IS 'Email пользователя';
COMMENT ON COLUMN users.password IS 'Хеш пароля (bcrypt)';
COMMENT ON COLUMN users.role IS 'Роль: USER или ADMIN';
COMMENT ON COLUMN users.notification_days IS 'За сколько дней до платежа отправлять уведомление';
COMMENT ON COLUMN users.created_at IS 'Дата создания учетной записи';

-- =============================================================================
-- Таблица categories
-- =============================================================================

CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'CUSTOM',
    created_by_user_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT categories_type_check CHECK (type IN ('SYSTEM', 'CUSTOM', 'LEGACY')),
    CONSTRAINT categories_created_by_fk FOREIGN KEY (created_by_user_id)
        REFERENCES users(id) ON DELETE SET NULL
);

COMMENT ON TABLE categories IS 'Категории подписок';
COMMENT ON COLUMN categories.id IS 'UUID категории';
COMMENT ON COLUMN categories.name IS 'Название категории';
COMMENT ON COLUMN categories.type IS 'Тип категории: SYSTEM (встроенная), CUSTOM (пользовательская), LEGACY (устаревшая)';
COMMENT ON COLUMN categories.created_by_user_id IS 'ID пользователя, создавшего категорию (NULL для системных)';
COMMENT ON COLUMN categories.created_at IS 'Дата создания категории';

-- =============================================================================
-- Таблица payments
-- =============================================================================

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    cost DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'RUB',
    billing_period_days INTEGER NOT NULL DEFAULT 30,
    next_billing_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT payments_cost_check CHECK (cost > 0),
    CONSTRAINT payments_currency_check CHECK (currency IN ('RUB', 'USD', 'EUR')),
    CONSTRAINT payments_billing_period_check CHECK (billing_period_days > 0)
);

COMMENT ON TABLE payments IS 'Платежная информация для подписок';
COMMENT ON COLUMN payments.id IS 'UUID платежа';
COMMENT ON COLUMN payments.cost IS 'Стоимость подписки';
COMMENT ON COLUMN payments.currency IS 'Валюта: RUB, USD, EUR';
COMMENT ON COLUMN payments.billing_period_days IS 'Период оплаты в днях';
COMMENT ON COLUMN payments.next_billing_date IS 'Дата следующего платежа';
COMMENT ON COLUMN payments.created_at IS 'Дата создания записи';

-- =============================================================================
-- Таблица subscriptions
-- =============================================================================

CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    category_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    payment_id UUID UNIQUE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT subscriptions_user_fk FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT subscriptions_category_fk FOREIGN KEY (category_id)
        REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT subscriptions_payment_fk FOREIGN KEY (payment_id)
        REFERENCES payments(id) ON DELETE CASCADE
);

COMMENT ON TABLE subscriptions IS 'Подписки пользователей';
COMMENT ON COLUMN subscriptions.id IS 'UUID подписки';
COMMENT ON COLUMN subscriptions.user_id IS 'ID пользователя-владельца';
COMMENT ON COLUMN subscriptions.category_id IS 'ID категории';
COMMENT ON COLUMN subscriptions.name IS 'Название подписки (сервиса)';
COMMENT ON COLUMN subscriptions.payment_id IS 'ID платежной информации (уникальная связь 1:1)';
COMMENT ON COLUMN subscriptions.is_active IS 'Флаг активности подписки';
COMMENT ON COLUMN subscriptions.created_at IS 'Дата создания подписки';

-- =============================================================================
-- Таблица notifications
-- =============================================================================

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    subscription_id UUID NOT NULL,
    notification_date TIMESTAMP NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    is_sent BOOLEAN NOT NULL DEFAULT FALSE,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT notifications_type_check CHECK (notification_type IN ('UPCOMING_PAYMENT', 'PAYMENT_SUCCESSFUL')),
    CONSTRAINT notifications_user_fk FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT notifications_subscription_fk FOREIGN KEY (subscription_id)
        REFERENCES subscriptions(id) ON DELETE CASCADE
);

COMMENT ON TABLE notifications IS 'Уведомления пользователям о платежах';
COMMENT ON COLUMN notifications.id IS 'UUID уведомления';
COMMENT ON COLUMN notifications.user_id IS 'ID пользователя-получателя';
COMMENT ON COLUMN notifications.subscription_id IS 'ID подписки';
COMMENT ON COLUMN notifications.notification_date IS 'Дата и время отправки уведомления';
COMMENT ON COLUMN notifications.notification_type IS 'Тип: UPCOMING_PAYMENT, PAYMENT_SUCCESSFUL';
COMMENT ON COLUMN notifications.is_sent IS 'Флаг отправки уведомления';
COMMENT ON COLUMN notifications.message IS 'Текст уведомления';
COMMENT ON COLUMN notifications.created_at IS 'Дата создания уведомления';