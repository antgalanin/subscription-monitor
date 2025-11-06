-- =============================================================================
-- Инициализация БД в JAVA MODE (для интеграции со Spring Boot)
-- =============================================================================

\echo '=== JAVA MODE ==='
\echo ''

\echo '=== Шаг 1/7: Создание БД и расширений ==='
\i create_database.sql

\echo '=== Шаг 2/7: Создание таблиц ==='
\c subscription_monitor
\i create_tables.sql

\echo '=== Шаг 3/7: Создание constraints ==='
\i constraints_java.sql

\echo '=== Шаг 4/7: Создание аналитических витрин ==='
\i create_views.sql

\echo '=== Шаг 5/7: Создание индексов ==='
\i create_indexes.sql

\echo '=== Шаг 6/7: Создание ролей ==='
\i create_roles.sql

\echo '=== Шаг 7/7: Инициализация данных ==='
\echo 'Данные будут созданы через DataInitializer.java при запуске приложения'
\echo ''

\echo '=== Инициализация JAVA MODE завершена ==='
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