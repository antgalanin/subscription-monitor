# PostgreSQL Database Scripts - Subscription Monitor

Скрипты инициализации и настройки базы данных PostgreSQL для системы мониторинга подписок.

## Режимы работы БД

База данных поддерживает **два режима**:

### 🟢 JAVA MODE - Интеграция со Spring Boot (рекомендуется)
- БД без триггеров
- Вся бизнес-логика в Java сервисах
- Используется в production

### 🔵 FULL MODE - Автономная БД (для курсовой ББД)
- Полная бизнес-логика на уровне PostgreSQL
- Триггеры для автоматического создания уведомлений
- Автоматическая обработка платежей
- Работает без Java приложения

## Структура файлов

```
sql/
├── init_java.sql            # [РЕКОМЕНДУЕТСЯ] Инициализация для Spring Boot
├── init_full.sql            # Инициализация для курсовой ББД
├── create_database.sql      # Создание БД и расширений
├── create_tables.sql        # Создание 5 таблиц
├── constraints_java.sql     # [JAVA MODE] Constraints без триггеров
├── constraints_full.sql     # [FULL MODE] Constraints + триггеры
├── create_views.sql         # Аналитические витрины (10 views + 1 materialized)
├── create_indexes.sql       # Индексы для оптимизации (28 индексов)
├── create_roles.sql         # Роли БД (2 роли)
├── create_rls_policies.sql  # Row Level Security (FULL MODE)
├── test_data.sql            # Тестовые данные (FULL MODE)
├── clean-database.sql       # Очистка БД
└── README.md                # Эта документация
```

## Быстрый старт

### 🟢 JAVA MODE (рекомендуется)

```bash
cd src/main/resources/sql
psql -U postgres -f init_java.sql
```

**Результат:**
- БД создана без триггеров
- Данные создаются через `DataInitializer.java` при запуске Spring Boot

### 🔵 FULL MODE (для курсовой ББД)

```bash
cd src/main/resources/sql
psql -U postgres -f init_full.sql
```

**Результат:**
- БД создана с триггерами и RLS
- Тестовые данные загружены
- Триггеры автоматически создают уведомления

### 🗑️ Удаление и очистка БД

**Полное удаление БД:**
```bash
cd src/main/resources/sql
psql -U postgres -c "SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = 'subscription_monitor';"
psql -U postgres -c "DROP DATABASE IF EXISTS subscription_monitor;"
```

**Очистка данных (структура сохраняется):**
```bash
cd src/main/resources/sql
psql -U postgres -d subscription_monitor -f clean-database.sql
```

## Архитектура БД

### Таблицы (5 шт)

| Таблица         | Описание                          | Ключевые поля                     |
|-----------------|-----------------------------------|-----------------------------------|
| `users`         | Пользователи системы              | username, email, role             |
| `categories`    | Категории подписок                | name, type (SYSTEM/CUSTOM)        |
| `payments`      | Платежи                           | cost, currency, next_billing_date |
| `subscriptions` | Подписки пользователей            | user_id, payment_id, is_active    |
| `notifications` | Уведомления                       | notification_type, is_sent        |

### Enums (реализованы через VARCHAR CHECK)

- **UserRole**: `USER`, `ADMIN`
- **CategoryType**: `SYSTEM`, `CUSTOM`, `LEGACY`
- **Currency**: `RUB`, `USD`, `EUR`
- **NotificationType**: `UPCOMING_PAYMENT`, `PAYMENT_SUCCESSFUL`

### Отношения

- `subscriptions.user_id` → `users.id` (Many-to-One)
- `subscriptions.category_id` → `categories.id` (Many-to-One)
- `subscriptions.payment_id` → `payments.id` (One-to-One, UNIQUE)
- `notifications.user_id` → `users.id` (Many-to-One)
- `notifications.subscription_id` → `subscriptions.id` (Many-to-One)

## Ключевые особенности

### UUID Primary Keys

Все таблицы используют UUID вместо BIGSERIAL:

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ...
);
```

### Триггеры (только в FULL MODE)

FULL MODE реализует полную бизнес-логику на уровне БД, идентичную Java-сервисам:

| Триггер | Событие | Описание |
|---------|---------|----------|
| `create_notifications_for_subscription()` | INSERT subscriptions | Создает 2 уведомления: PAYMENT_SUCCESSFUL (is_sent=true) и UPCOMING_PAYMENT (is_sent=false) |
| `handle_subscription_changes()` | UPDATE subscriptions | **Деактивация**: удаляет is_sent=false UPCOMING_PAYMENT<br>**Активация**: создает оба уведомления<br>**Изменения**: обновляет message и даты |
| `handle_payment_changes()` | UPDATE payments | Обновляет уведомления при изменении cost/currency/next_billing_date |
| CASCADE DELETE | DELETE subscriptions | Автоматически удаляет все связанные уведомления |

**В JAVA MODE триггеров нет** - вся логика в `SubscriptionService.java`.

### Аналитические витрины

| View                          | Описание                                                    |
|-------------------------------|-------------------------------------------------------------|
| `user_subscriptions_summary`  | Сводка подписок с нормализацией к месячным тратам         |
| `upcoming_payments`           | Предстоящие платежи с категоризацией срочности             |
| `category_statistics`         | Глобальная статистика по категориям (все пользователи)     |
| `user_category_statistics`    | Персонализированная статистика категорий по пользователям  |
| `user_activity`               | Активность пользователей                                    |
| `top_subscriptions`           | Топ популярных подписок                                     |
| `notification_analysis`       | Анализ уведомлений                                          |
| `urgent_payments`             | Срочные платежи (ближайшие 7 дней)                         |
| `spending_trends`             | Прогноз расходов на 6 месяцев                              |
| `expensive_subscriptions`     | Самые дорогие подписки с нормализацией к месячной стоимости |
| `monthly_statistics`          | Месячная статистика (MATERIALIZED VIEW)                     |

**Ключевая особенность:**
Витрина `user_subscriptions_summary` автоматически нормализует стоимость подписок к месячным тратам:
- 1 день → `cost × 30` (100₽/день = 3000₽/месяц)
- 7 дней → `cost × 4.3` (500₽/неделю = 2150₽/месяц)
- 30 дней → `cost` (1000₽/месяц)
- 90 дней → `cost / 3` (3000₽/квартал = 1000₽/месяц)
- 365 дней → `cost / 12` (12000₽/год = 1000₽/месяц)
- Другие периоды → `cost × 30 / billing_period_days`

GUI и REST API получают данные напрямую из этой витрины через `AnalyticsService.getUserStatistics()`.

### Индексы

- **28 B-tree индексов** для быстрых запросов
- **Partial индексы** для активных подписок и неотправленных уведомлений
- **BRIN индексы** для временных данных (created_at)
- **Composite индексы** для частых комбинаций фильтров

### Row Level Security (RLS)

RLS включен для таблиц `subscriptions` и `notifications`:

```sql
-- Пример использования
SELECT set_current_user_id('uuid-пользователя');
SELECT * FROM subscriptions; -- Вернет только подписки текущего пользователя
```

## Роли БД

### 1. subscription_app (для Spring Boot)
- Полный доступ к таблицам (CRUD)
- SELECT на аналитических витринах
- Пароль: `Sgj$_tKW0B2N`

### 2. subscription_admin (для DBA)
- SUPERUSER для администрирования
- Пароль: `h_Es+~aY~4iA`

## Тестовые данные

После выполнения `test_data.sql` будет создано:

- 5 пользователей (1 ADMIN, 4 USER)
- 6 категорий (4 SYSTEM, 2 CUSTOM)
- 9 подписок в разных валютах (RUB, USD, EUR)
- Автоматически созданные уведомления через триггеры

## Настройка Spring Boot

### application.properties

```properties
spring.datasource.username=subscription_app
spring.datasource.password=Sgj$_tKW0B2N
```

### Использование витрин в Java

```java
@Service
public class AnalyticsService {
    private final JdbcTemplate jdbcTemplate;

    public UserStatisticsDto getUserStatistics(UUID userId) {
        String sql = "SELECT * FROM analytics.user_subscriptions_summary WHERE user_id = ?";
        return jdbcTemplate.query(sql, new UserStatisticsRowMapper(), userId).get(0);
    }
}
```
