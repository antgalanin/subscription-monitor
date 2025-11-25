-- =============================================================================
-- Создание ролей и настройка прав доступа
-- =============================================================================

-- =============================================================================
-- Роль 1: subscription_app - для Spring Boot приложения
-- =============================================================================

CREATE ROLE subscription_app WITH LOGIN PASSWORD 'Sgj$_tKW0B2N';

COMMENT ON ROLE subscription_app IS
'Роль для Spring Boot приложения с полным доступом к операционным таблицам';

GRANT CONNECT ON DATABASE subscription_monitor TO subscription_app;
GRANT USAGE ON SCHEMA public TO subscription_app;
GRANT USAGE ON SCHEMA analytics TO subscription_app;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO subscription_app;

GRANT SELECT ON ALL TABLES IN SCHEMA analytics TO subscription_app;

GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO subscription_app;

-- =============================================================================
-- Роль 2: subscription_admin - для администрирования БД
-- =============================================================================

CREATE ROLE subscription_admin WITH LOGIN PASSWORD 'h_Es+~aY~4iA' SUPERUSER;

COMMENT ON ROLE subscription_admin IS
'Роль администратора БД для обслуживания, резервного копирования и мониторинга';

-- =============================================================================
-- Автоматическое применение прав для новых объектов
-- =============================================================================

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT ALL ON TABLES TO subscription_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA analytics
GRANT SELECT ON TABLES TO subscription_app;