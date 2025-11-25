# PostgreSQL Database Scripts - Subscription Monitor

Скрипты инициализации и настройки базы данных PostgreSQL для системы мониторинга подписок.

## Архитектура БД

База данных интегрирована со Spring Boot приложением:
- Вся бизнес-логика реализована в Java сервисах
- Enums реализованы через VARCHAR + CHECK constraints (для совместимости с JPA/Hibernate)
- Тестовые данные создаются автоматически через `DataInitializer.java` при запуске приложения

## Структура файлов

```
sql/
├── init.sql                 # Главный скрипт инициализации
├── create_database.sql      # Создание БД и расширений
├── create_tables.sql        # Таблицы с VARCHAR + CHECK для совместимости с JPA
├── constraints.sql          # Constraints (без триггеров)
├── create_views.sql         # Аналитические витрины (3 views для AnalyticsService)
├── create_indexes.sql       # Индексы для оптимизации (28 индексов)
├── create_roles.sql         # Роли БД (2 роли)
├── clean-database.sql       # Очистка данных БД
└── README.md                # Эта документация
```

## Быстрый старт

### Инициализация БД

```bash
cd src/main/resources/sql
psql -U postgres -f init.sql
```

**Результат:**
- БД создана без триггеров
- Данные создаются через `DataInitializer.java` при запуске Spring Boot

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

### Enums

| Enum | Значения | Реализация |
|------|----------|------------|
| **UserRole** | `USER`, `ADMIN` | VARCHAR + CHECK |
| **CategoryType** | `SYSTEM`, `CUSTOM`, `LEGACY` | VARCHAR + CHECK |
| **Currency** | `RUB`, `USD`, `EUR` | VARCHAR + CHECK |
| **NotificationType** | `UPCOMING_PAYMENT`, `PAYMENT_SUCCESSFUL` | VARCHAR + CHECK |

**Реализация:** VARCHAR + CHECK constraints (для совместимости с JPA/Hibernate)

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

### Аналитические витрины

БД содержит 3 аналитические витрины, используемые в `AnalyticsService.java`:

| View                          | Описание                                                    |
|-------------------------------|-------------------------------------------------------------|
| `user_subscriptions_summary`  | Сводка подписок с нормализацией к месячным тратам         |
| `upcoming_payments`           | Предстоящие платежи с категоризацией срочности             |
| `user_category_statistics`    | Персонализированная статистика категорий по пользователям  |

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

## Роли БД

### 1. subscription_app (для Spring Boot)
- Полный доступ к таблицам (CRUD)
- SELECT на аналитических витринах
- Пароль: `Sgj$_tKW0B2N`

### 2. subscription_admin (для DBA)
- SUPERUSER для администрирования
- Пароль: `h_Es+~aY~4iA`

## Тестовые данные

Тестовые данные автоматически создаются при первом запуске Spring Boot приложения через `DataInitializer.java`:

- 1 пользователь ADMIN (логин: `admin`, пароль: `admin123`)
- 4 системные категории (SYSTEM)
- Несколько тестовых подписок с разными валютами (RUB, USD, EUR)
- Автоматически созданные уведомления через `SubscriptionService`

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
