-- =============================================================================
-- Создание базы данных для системы мониторинга подписок
-- =============================================================================

CREATE DATABASE subscription_monitor
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

\c subscription_monitor

-- =============================================================================
-- Установка расширения для UUID
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

COMMENT ON EXTENSION "uuid-ossp" IS 'Поддержка UUID для генерации уникальных идентификаторов';

-- =============================================================================
-- Создание схем
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS public;
CREATE SCHEMA IF NOT EXISTS analytics;

COMMENT ON SCHEMA public IS 'Основная схема для операционных таблиц системы мониторинга подписок';
COMMENT ON SCHEMA analytics IS 'Схема для аналитических витрин данных и материализованных представлений';

ALTER DATABASE subscription_monitor SET search_path TO public, analytics;