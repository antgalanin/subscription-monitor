-- =============================================================================
-- Constraints
-- =============================================================================

CREATE UNIQUE INDEX idx_subscriptions_unique_active
    ON subscriptions(user_id, name)
    WHERE is_active = TRUE;

COMMENT ON INDEX idx_subscriptions_unique_active IS
'Partial unique индекс: пользователь не может иметь две активные подписки с одним именем';