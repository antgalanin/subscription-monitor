-- =============================================================================
-- Row Level Security (RLS) - Политики доступа на уровне строк
-- =============================================================================

ALTER TABLE subscriptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

-- =============================================================================
-- Политики RLS для таблицы subscriptions
-- =============================================================================

CREATE POLICY subscriptions_app_rls_policy ON subscriptions
    FOR ALL
    TO subscription_app
    USING (
        user_id = current_setting('app.current_user_id', true)::UUID
        OR current_setting('app.current_user_id', true) IS NULL
    )
    WITH CHECK (
        user_id = current_setting('app.current_user_id', true)::UUID
        OR current_setting('app.current_user_id', true) IS NULL
    );

COMMENT ON POLICY subscriptions_app_rls_policy ON subscriptions IS
'Приложение видит только данные текущего пользователя (app.current_user_id). NULL = полный доступ для системных операций';

CREATE POLICY subscriptions_admin_all_policy ON subscriptions
    FOR ALL
    TO subscription_admin
    USING (true)
    WITH CHECK (true);

COMMENT ON POLICY subscriptions_admin_all_policy ON subscriptions IS
'Администраторы имеют полный доступ (bypass RLS)';

-- =============================================================================
-- Политики RLS для таблицы notifications
-- =============================================================================

CREATE POLICY notifications_app_rls_policy ON notifications
    FOR ALL
    TO subscription_app
    USING (
        user_id = current_setting('app.current_user_id', true)::UUID
        OR current_setting('app.current_user_id', true) IS NULL
    )
    WITH CHECK (
        user_id = current_setting('app.current_user_id', true)::UUID
        OR current_setting('app.current_user_id', true) IS NULL
    );

COMMENT ON POLICY notifications_app_rls_policy ON notifications IS
'Приложение видит только уведомления текущего пользователя';

CREATE POLICY notifications_admin_all_policy ON notifications
    FOR ALL
    TO subscription_admin
    USING (true)
    WITH CHECK (true);

COMMENT ON POLICY notifications_admin_all_policy ON notifications IS
'Администраторы имеют полный доступ';

-- =============================================================================
-- Вспомогательные функции для работы с RLS
-- =============================================================================

CREATE OR REPLACE FUNCTION set_current_user_id(p_user_id UUID)
RETURNS void AS $$
BEGIN
    PERFORM set_config('app.current_user_id', p_user_id::TEXT, false);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION set_current_user_id(UUID) IS
'Устанавливает UUID текущего пользователя для RLS политик';

CREATE OR REPLACE FUNCTION get_current_user_id()
RETURNS UUID AS $$
BEGIN
    RETURN current_setting('app.current_user_id', true)::UUID;
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION get_current_user_id() IS
'Возвращает UUID текущего пользователя из настроек сессии';

GRANT EXECUTE ON FUNCTION set_current_user_id(UUID) TO subscription_app;
GRANT EXECUTE ON FUNCTION get_current_user_id() TO subscription_app;