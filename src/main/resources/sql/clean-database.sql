-- =============================================================================
-- Очистка БД (структура сохраняется)
-- =============================================================================

SET session_replication_role = 'replica';

TRUNCATE TABLE notifications CASCADE;
TRUNCATE TABLE subscriptions CASCADE;
TRUNCATE TABLE payments CASCADE;
TRUNCATE TABLE categories CASCADE;
TRUNCATE TABLE users CASCADE;

SET session_replication_role = 'origin';

\echo ''
\echo '=== База данных очищена ==='
SELECT 'users' AS table_name, COUNT(*) AS records FROM users
UNION ALL SELECT 'categories', COUNT(*) FROM categories
UNION ALL SELECT 'payments', COUNT(*) FROM payments
UNION ALL SELECT 'subscriptions', COUNT(*) FROM subscriptions
UNION ALL SELECT 'notifications', COUNT(*) FROM notifications
ORDER BY table_name;