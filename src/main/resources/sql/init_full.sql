-- =============================================================================
-- Инициализация БД в FULL MODE (автономная БД с триггерами и RLS)
-- =============================================================================

\echo '=== FULL MODE ==='
\echo ''

\echo '=== Шаг 1/8: Создание БД и расширений ==='
\i create_database.sql

\echo '=== Шаг 2/8: Создание таблиц ==='
\c subscription_monitor
\i create_tables.sql

\echo '=== Шаг 3/8: Создание ограничений и триггеров ==='
\i constraints_full.sql

\echo '=== Шаг 4/8: Создание аналитических витрин ==='
\i create_views.sql

\echo '=== Шаг 5/8: Создание индексов ==='
\i create_indexes.sql

\echo '=== Шаг 6/8: Создание ролей ==='
\i create_roles.sql

\echo '=== Шаг 7/8: Создание RLS политик ==='
\i create_rls_policies.sql

\echo '=== Шаг 8/8: Загрузка тестовых данных ==='
\i test_data.sql

\echo ''
\echo '=== Инициализация завершена ==='
\echo ''
\echo 'Статистика БД:'
SELECT 'users' AS table_name, COUNT(*) AS records FROM users
UNION ALL SELECT 'categories', COUNT(*) FROM categories
UNION ALL SELECT 'payments', COUNT(*) FROM payments
UNION ALL SELECT 'subscriptions', COUNT(*) FROM subscriptions
UNION ALL SELECT 'notifications', COUNT(*) FROM notifications
ORDER BY table_name;

\echo ''
\echo 'Аналитические витрины:'
SELECT schemaname, viewname FROM pg_views
WHERE schemaname = 'analytics'
ORDER BY viewname;

\echo ''
\echo 'Роли:'
SELECT rolname, rolsuper, rolcanlogin FROM pg_roles
WHERE rolname LIKE 'subscription%'
ORDER BY rolname;

\echo ''
\echo 'Триггеры:'
SELECT tgname, tgtype, proname FROM pg_trigger t
JOIN pg_proc p ON t.tgfoid = p.oid
WHERE tgname LIKE 'trigger_%'
ORDER BY tgname;

\echo ''
\echo 'RLS политики:'
SELECT COUNT(*) as count FROM pg_policies;