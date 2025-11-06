-- Constraints и триггеры (FULL MODE)

-- =============================================================================
-- CASCADE DELETE для notifications при удалении subscriptions
-- =============================================================================

ALTER TABLE notifications DROP CONSTRAINT IF EXISTS fk_notifications_subscription;
ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_subscription
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE;

COMMENT ON CONSTRAINT fk_notifications_subscription ON notifications IS
'При удалении подписки автоматически удаляются все связанные уведомления';

-- =============================================================================
-- Триггер 1: Создание 2 уведомлений при INSERT subscriptions
-- =============================================================================

CREATE OR REPLACE FUNCTION create_notifications_for_subscription()
RETURNS TRIGGER AS $$
DECLARE
    v_user_id UUID;
    v_notification_days INTEGER;
    v_cost DECIMAL(10,2);
    v_currency VARCHAR(3);
    v_next_billing_date DATE;
    v_billing_period_days INTEGER;
    v_last_payment_date DATE;
    v_notification_date DATE;
BEGIN
    IF NEW.is_active = FALSE THEN
        RETURN NEW;
    END IF;

    SELECT s.user_id, u.notification_days, p.cost, p.currency, p.next_billing_date, p.billing_period_days
    INTO v_user_id, v_notification_days, v_cost, v_currency, v_next_billing_date, v_billing_period_days
    FROM subscriptions s
    JOIN users u ON s.user_id = u.id
    JOIN payments p ON s.payment_id = p.id
    WHERE s.id = NEW.id;

    IF v_user_id IS NULL OR v_next_billing_date IS NULL THEN
        RETURN NEW;
    END IF;

    v_last_payment_date := v_next_billing_date - INTERVAL '1 day' * COALESCE(v_billing_period_days, 30);
    v_notification_date := v_next_billing_date - INTERVAL '1 day' * v_notification_days;

    INSERT INTO notifications (
        id, user_id, subscription_id, notification_date, notification_type, is_sent, message
    ) VALUES (
        uuid_generate_v4(),
        v_user_id,
        NEW.id,
        v_last_payment_date::TIMESTAMP,
        'PAYMENT_SUCCESSFUL',
        TRUE,
        'Платеж выполнен успешно: ' || NEW.name || ' - ' || v_cost || ' ' || v_currency || '. Следующее списание: ' || v_next_billing_date
    );

    INSERT INTO notifications (
        id, user_id, subscription_id, notification_date, notification_type, is_sent, message
    ) VALUES (
        uuid_generate_v4(),
        v_user_id,
        NEW.id,
        v_notification_date::TIMESTAMP,
        'UPCOMING_PAYMENT',
        FALSE,
        'Предстоящий платеж: ' || NEW.name || ' - ' || v_cost || ' ' || v_currency || ' (через ' || v_notification_days || ' дн.)'
    );

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION create_notifications_for_subscription() IS
'Создает 2 уведомления при создании активной подписки: PAYMENT_SUCCESSFUL (is_sent=true) и UPCOMING_PAYMENT (is_sent=false)';

CREATE TRIGGER trigger_create_notifications_for_subscription
    AFTER INSERT ON subscriptions
    FOR EACH ROW
    EXECUTE FUNCTION create_notifications_for_subscription();

-- =============================================================================
-- Триггер 2: Обработка изменений в subscriptions
-- =============================================================================

CREATE OR REPLACE FUNCTION handle_subscription_changes()
RETURNS TRIGGER AS $$
DECLARE
    v_user_id UUID;
    v_notification_days INTEGER;
    v_cost DECIMAL(10,2);
    v_currency VARCHAR(3);
    v_next_billing_date DATE;
    v_billing_period_days INTEGER;
    v_last_payment_date DATE;
    v_notification_date DATE;
BEGIN
    IF OLD.is_active = TRUE AND NEW.is_active = FALSE THEN
        DELETE FROM notifications
        WHERE subscription_id = NEW.id
          AND is_sent = FALSE
          AND notification_type = 'UPCOMING_PAYMENT';
        RETURN NEW;
    END IF;

    IF OLD.is_active = FALSE AND NEW.is_active = TRUE THEN
        SELECT s.user_id, u.notification_days, p.cost, p.currency, p.next_billing_date, p.billing_period_days
        INTO v_user_id, v_notification_days, v_cost, v_currency, v_next_billing_date, v_billing_period_days
        FROM subscriptions s
        JOIN users u ON s.user_id = u.id
        JOIN payments p ON s.payment_id = p.id
        WHERE s.id = NEW.id;

        IF v_user_id IS NOT NULL AND v_next_billing_date IS NOT NULL THEN
            v_last_payment_date := v_next_billing_date - INTERVAL '1 day' * COALESCE(v_billing_period_days, 30);
            v_notification_date := v_next_billing_date - INTERVAL '1 day' * v_notification_days;

            IF NOT EXISTS (SELECT 1 FROM notifications WHERE subscription_id = NEW.id AND notification_type = 'PAYMENT_SUCCESSFUL') THEN
                INSERT INTO notifications (
                    id, user_id, subscription_id, notification_date, notification_type, is_sent, message
                ) VALUES (
                    uuid_generate_v4(), v_user_id, NEW.id, v_last_payment_date::TIMESTAMP, 'PAYMENT_SUCCESSFUL', TRUE,
                    'Платеж выполнен успешно: ' || NEW.name || ' - ' || v_cost || ' ' || v_currency || '. Следующее списание: ' || v_next_billing_date
                );
            END IF;

            IF NOT EXISTS (SELECT 1 FROM notifications WHERE subscription_id = NEW.id AND notification_type = 'UPCOMING_PAYMENT') THEN
                INSERT INTO notifications (
                    id, user_id, subscription_id, notification_date, notification_type, is_sent, message
                ) VALUES (
                    uuid_generate_v4(), v_user_id, NEW.id, v_notification_date::TIMESTAMP, 'UPCOMING_PAYMENT', FALSE,
                    'Предстоящий платеж: ' || NEW.name || ' - ' || v_cost || ' ' || v_currency || ' (через ' || v_notification_days || ' дн.)'
                );
            END IF;
        END IF;

        RETURN NEW;
    END IF;

    IF NEW.is_active = TRUE AND (OLD.name <> NEW.name OR OLD.payment_id <> NEW.payment_id) THEN
        SELECT s.user_id, u.notification_days, p.cost, p.currency, p.next_billing_date, p.billing_period_days
        INTO v_user_id, v_notification_days, v_cost, v_currency, v_next_billing_date, v_billing_period_days
        FROM subscriptions s
        JOIN users u ON s.user_id = u.id
        JOIN payments p ON s.payment_id = p.id
        WHERE s.id = NEW.id;

        IF v_user_id IS NOT NULL AND v_next_billing_date IS NOT NULL THEN
            v_last_payment_date := v_next_billing_date - INTERVAL '1 day' * COALESCE(v_billing_period_days, 30);
            v_notification_date := v_next_billing_date - INTERVAL '1 day' * v_notification_days;

            UPDATE notifications
            SET message = 'Платеж выполнен успешно: ' || NEW.name || ' - ' || v_cost || ' ' || v_currency || '. Следующее списание: ' || v_next_billing_date,
                notification_date = v_last_payment_date::TIMESTAMP
            WHERE subscription_id = NEW.id
              AND notification_type = 'PAYMENT_SUCCESSFUL';

            UPDATE notifications
            SET message = 'Предстоящий платеж: ' || NEW.name || ' - ' || v_cost || ' ' || v_currency || ' (через ' || v_notification_days || ' дн.)',
                notification_date = v_notification_date::TIMESTAMP,
                is_sent = CASE WHEN v_notification_date::TIMESTAMP > CURRENT_TIMESTAMP THEN FALSE ELSE TRUE END
            WHERE subscription_id = NEW.id
              AND notification_type = 'UPCOMING_PAYMENT';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION handle_subscription_changes() IS
'Обрабатывает изменения подписки: деактивацию (удаляет is_sent=false), активацию (создает уведомления), изменения данных (обновляет уведомления)';

CREATE TRIGGER trigger_handle_subscription_changes
    AFTER UPDATE ON subscriptions
    FOR EACH ROW
    EXECUTE FUNCTION handle_subscription_changes();

-- =============================================================================
-- Триггер 3: Обработка изменений в payments
-- =============================================================================

CREATE OR REPLACE FUNCTION handle_payment_changes()
RETURNS TRIGGER AS $$
DECLARE
    v_subscription RECORD;
    v_user RECORD;
    v_last_payment_date DATE;
    v_notification_date DATE;
BEGIN
    IF OLD.cost = NEW.cost AND OLD.currency = NEW.currency AND OLD.next_billing_date = NEW.next_billing_date AND OLD.billing_period_days = NEW.billing_period_days THEN
        RETURN NEW;
    END IF;

    FOR v_subscription IN
        SELECT s.id, s.name, s.user_id, s.is_active
        FROM subscriptions s
        WHERE s.payment_id = NEW.id AND s.is_active = TRUE
    LOOP
        SELECT u.notification_days INTO v_user FROM users u WHERE u.id = v_subscription.user_id;

        IF v_user IS NOT NULL AND NEW.next_billing_date IS NOT NULL THEN
            v_last_payment_date := NEW.next_billing_date - INTERVAL '1 day' * COALESCE(NEW.billing_period_days, 30);
            v_notification_date := NEW.next_billing_date - INTERVAL '1 day' * v_user.notification_days;

            UPDATE notifications
            SET message = 'Платеж выполнен успешно: ' || v_subscription.name || ' - ' || NEW.cost || ' ' || NEW.currency || '. Следующее списание: ' || NEW.next_billing_date,
                notification_date = v_last_payment_date::TIMESTAMP
            WHERE subscription_id = v_subscription.id
              AND notification_type = 'PAYMENT_SUCCESSFUL';

            UPDATE notifications
            SET message = 'Предстоящий платеж: ' || v_subscription.name || ' - ' || NEW.cost || ' ' || NEW.currency || ' (через ' || v_user.notification_days || ' дн.)',
                notification_date = v_notification_date::TIMESTAMP,
                is_sent = CASE WHEN v_notification_date::TIMESTAMP > CURRENT_TIMESTAMP THEN FALSE ELSE TRUE END
            WHERE subscription_id = v_subscription.id
              AND notification_type = 'UPCOMING_PAYMENT';
        END IF;
    END LOOP;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION handle_payment_changes() IS
'Обновляет уведомления при изменении данных платежа (cost, currency, next_billing_date)';

CREATE TRIGGER trigger_handle_payment_changes
    AFTER UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION handle_payment_changes();

-- =============================================================================
-- Partial unique индекс для активных подписок
-- =============================================================================

CREATE UNIQUE INDEX idx_subscriptions_unique_active
    ON subscriptions(user_id, name)
    WHERE is_active = TRUE;

COMMENT ON INDEX idx_subscriptions_unique_active IS
'Partial unique индекс: пользователь не может иметь две активные подписки с одним именем';
