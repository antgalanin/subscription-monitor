-- =====================================================
-- Script: clean-database.sql
-- Description: Cleans all data from the database tables
-- Usage: psql -U postgres -d subscription_monitor -f src/main/resources/sql/clean-database.sql
-- =====================================================

-- Disable triggers temporarily to avoid FK constraint issues
SET session_replication_role = 'replica';

-- Clean all tables in reverse dependency order
TRUNCATE TABLE payments CASCADE;
TRUNCATE TABLE notifications CASCADE;
TRUNCATE TABLE subscriptions CASCADE;
TRUNCATE TABLE categories CASCADE;
TRUNCATE TABLE users CASCADE;

-- Re-enable triggers
SET session_replication_role = 'origin';

-- Display success message
SELECT 'Database cleaned successfully! All tables are now empty.' AS status;
