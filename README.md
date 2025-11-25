# Subscription Monitor

Система мониторинга персональных подписок с автоматическими уведомлениями и обработкой платежей.

## Оглавление

- [Описание проекта](#описание-проекта)
- [Архитектура](#архитектура)
- [База данных](#база-данных)
- [REST API](#rest-api)
- [Обработка исключений](#обработка-исключений)
- [Безопасность и роли пользователей](#безопасность-и-роли-пользователей)
- [Автоматизация и планировщики](#автоматизация-и-планировщики)
- [Графический интерфейс (GUI)](#графический-интерфейс-gui)
- [Логирование](#логирование)
- [Быстрый старт](#быстрый-старт)
- [Структура проекта](#структура-проекта)

## Описание проекта

**Subscription Monitor** — это клиент-серверное приложение для управления персональными подписками с автоматическим мониторингом предстоящих платежей.

### Ключевые возможности

- Управление подписками с поддержкой различных валют (RUB, USD, EUR)
- Автоматические уведомления о предстоящих и выполненных платежах
- Категоризация подписок (системные, пользовательские и устаревшие категории)
- Аналитика расходов с нормализацией к месячным тратам
- Автоматическая обработка просроченных платежей
- REST API с документацией Swagger
- Графический интерфейс на Swing с современным дизайном
- Ролевая модель доступа (USER, ADMIN)
- Интеграция с PostgreSQL

### Стек технологий

- **Backend**: Java 24, Spring Boot 3.4.2, Spring Security, Spring Data JPA
- **База данных**: PostgreSQL 17
- **Frontend**: Java Swing, FlatLaf
- **REST API**: Spring Web, SpringDoc OpenAPI (Swagger UI)
- **Валидация**: Jakarta Validation
- **Логирование**: Logback
- **Сборка**: Maven
- **Безопасность**: BCrypt password encoding, HTTP Basic Authentication

## Архитектура

### Модели данных

Все модели наследуют: `id` (UUID), `createdAt` (LocalDateTime)

- **User** — Пользователь системы
    - `username` (String, unique), `email` (String, unique) — уникальные строки
    - `password` (String) — хешированный пароль
    - `role` (UserRole) — роль пользователя
    - `notificationDays` (Integer) — за сколько дней уведомлять

- **Category** — Категория подписки
    - `name` (String) — название категории
    - `type` (CategoryType) — тип категории
    - `createdByUserId` (UUID, nullable) — создатель

- **Payment** — Информация о платеже
    - `cost` (BigDecimal) — стоимость
    - `currency` (Currency) — валюта
    - `billingPeriodDays` (Integer) — период оплаты в днях
    - `nextBillingDate` (LocalDate) — дата следующего списания

- **Subscription** — Подписка пользователя
    - `userId` (UUID), `categoryId` (UUID) — связи
    - `name` (String) — название подписки
    - `payment` (Payment) — объект Payment
    - `isActive` (Boolean) — активна ли подписка

- **Notification** — Уведомление о платеже
    - `userId` (UUID), `subscriptionId` (UUID) — связи
    - `notificationDate` (LocalDateTime) — когда уведомить
    - `type` (NotificationType) — тип уведомления
    - `isSent` (Boolean) — отправлено ли уведомление
    - `message` (String) — текст уведомления

### Перечисления (Enums)

| Enum | Значения | Описание |
|------|----------|----------|
| `UserRole` | `USER`, `ADMIN` | Роль пользователя в системе |
| `CategoryType` | `SYSTEM`, `CUSTOM`, `LEGACY` | Тип категории (системная, пользовательская, устаревшая) |
| `Currency` | `RUB`, `USD`, `EUR` | Валюта платежа |
| `NotificationType` | `UPCOMING_PAYMENT`, `PAYMENT_SUCCESSFUL` | Тип уведомления (предстоящий платеж, успешное списание) |

**Реализация в БД:** VARCHAR + CHECK constraints (для совместимости с JPA/Hibernate)

### Связи между сущностями

| Сущности                       | Тип связи | Поле связи | Особенности                                     |
|--------------------------------|-----------|------------|-------------------------------------------------|
| `User`, `Subscription`         | 1:N | `Subscription.userId` | Один пользователь ↔ много подписок              |
| `Category`, `Subscription`     | 1:N | `Subscription.categoryId` | Одна категория ↔ много подписок                 |
| `Subscription`, `Payment`      | 1:1 | `Subscription.payment` | Один платеж ↔ одна подписка                     |
| `User`, `Notification`         | 1:N | `Notification.userId` | Один пользователь ↔ много уведомлений           |
| `Subscription`, `Notification` | 1:N | `Notification.subscriptionId` | Одна подписка ↔ много уведомлений               |

### Каскадные операции при удалении

| Удаляемая сущность | Связанная сущность | Действие | Механизм реализации |
|--------------------|--------------------|----------|---------------------|
| User | Subscription | Удаляются все подписки пользователя | SQL: ON DELETE CASCADE |
| User | Notification | Удаляются все уведомления пользователя | SQL: ON DELETE CASCADE |
| User | Category (CUSTOM) | Категории сохраняются, но теряют владельца (created_by_user_id = NULL) | SQL: ON DELETE SET NULL |
| Subscription | Payment | Удаляется связанный платеж | JPA: CascadeType.ALL |
| Subscription | Notification | Удаляются все уведомления подписки | SQL: ON DELETE CASCADE |
| Category | Subscription | Удаление запрещено при наличии активных подписок | SQL: ON DELETE RESTRICT |
| Payment | Subscription | Удаляется подписка | SQL: ON DELETE CASCADE |
| Payment | Notification | Удаляются уведомления через каскад от Subscription | SQL: ON DELETE CASCADE |

### Уникальные ограничения

- **Уникальность username и email** — каждый пользователь имеет уникальные логин и адрес электронной почты
- **Уникальность активных подписок** — пользователь не может создать две активные подписки с одинаковым названием

## База данных

Проект использует PostgreSQL для хранения данных:

- Вся бизнес-логика реализована в Java сервисах
- Enums реализованы через VARCHAR + CHECK constraints (для совместимости с JPA/Hibernate)
- Тестовые данные создаются автоматически через `DataInitializer.java` при запуске приложения

Подробная документация по базе данных доступна в [README.md](src/main/resources/sql/README.md)

## REST API

REST API разделен на 7 групп контроллеров:

| Контроллер | Назначение | Основные операции                                                                                                                                                      |
|------------|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `AuthController` | Аутентификация и регистрация | Регистрация новых пользователей, получение данных текущего пользователя, смена email, изменение пароля                                                                 |
| `UserController` | Управление пользователями | Создание, просмотр, обновление, удаление пользователей <br/>Фильтрация по ролям                                                                                        |
| `CategoryController` | Управление категориями | Создание, просмотр, обновление, удаление категорий <br/> Фильтрация по типам <br/> Контроль доступа для пользовательских категорий                                     |
| `SubscriptionController` | Управление подписками | Создание, просмотр, обновление, удаление подписок <br/> Фильтрация по пользователям, категориям, активности <br/> Атомарное обновление с платежами                     |
| `NotificationController` | Управление уведомлениями | Создание, просмотр, обновление, удаление уведомлений <br/> Фильтрация по пользователям, подпискам, статусу отправки <br/> Получение ожидающих и полученных уведомлений |
| `PaymentController` | Управление платежами | Создание, просмотр, обновление, удаление платежной информации <br/> Фильтрация по валютам                                                                              |
| `AnalyticsController` | Аналитика и статистика | Сводная статистика пользователя (общие расходы, количество подписок), статистика по категориям, предстоящие платежи с индикаторами срочности                           |

### Swagger документация

REST API документирован с помощью **SpringDoc OpenAPI 3** (Swagger UI).

**Адрес:**
```
http://localhost:8080/swagger-ui.html
```

**Возможности Swagger UI:**
- Интерактивная документация всех 7 контроллеров
- Примеры JSON для запросов и ответов
- Описание всех параметров, заголовков и тел запросов
- Документация кодов ответов
- Тестирование эндпоинтов прямо из браузера
- HTTP Basic Authentication через кнопку "Authorize"

## Обработка исключений

### Backend исключения

Иерархия исключений построена на трех базовых классах:

| Базовый класс | Наследуется от | Назначение |
|---------------|----------------|------------|
| `BaseException` | `Exception` | Корневой класс с кодом ошибки и аргументами |
| `EntityNotFoundException` | `BaseException` | Базовый класс для NotFound исключений (404) |
| `ValidationException` | `BaseException` | Базовый класс для валидационных исключений (400) |

**NotFound исключения** (404) — наследуются от `EntityNotFoundException`:

- `UserNotFoundException`, `CategoryNotFoundException`, `SubscriptionNotFoundException`, `NotificationNotFoundException`, `PaymentNotFoundException`

**Validation исключения** (400) — наследуются от `ValidationException`:

- `UserValidationException`, `CategoryValidationException`, `SubscriptionValidationException`, `NotificationValidationException`, `PaymentValidationException`

**Специальное исключение** (410) — наследуются от `BaseException`:

- `LegacyCategoryException` — попытка изменения/удаления LEGACY категории

**HTTP коды ответов:**

| Код | Тип | Когда возвращается |
|-----|-----|-------------------|
| 200 | OK | Успешное выполнение GET/PUT запроса |
| 201 | Created | Успешное создание ресурса (POST) |
| 204 | No Content | Успешное удаление ресурса (DELETE) |
| 400 | Bad Request | Ошибка валидации данных (ValidationException) |
| 401 | Unauthorized | Требуется аутентификация |
| 403 | Forbidden | Нет прав доступа (например, USER пытается удалить SYSTEM категорию) |
| 404 | Not Found | Ресурс не найден (EntityNotFoundException) |
| 410 | Gone | Ресурс устарел (LegacyCategoryException) |
| 500 | Internal Server Error | Внутренняя ошибка сервера |

**Пример ErrorResponse:**

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User with id 123... not found",
  "code": "USER_NOT_FOUND",
  "timestamp": "2025-01-05T12:30:00",
  "path": "/api/users/123..."
}
```

### GUI исключения

GUI использует `ApiException` для обработки ошибок REST API:

| Метод | Возвращает | Назначение |
|-------|------------|------------|
| `hasErrorCode(String code)` | `boolean` | Проверка конкретного кода ошибки |
| `isClientError()` | `boolean` | Проверка ошибки 4xx |
| `isServerError()` | `boolean` | Проверка ошибки 5xx |
| `getUserFriendlyMessage()` | `String` | Получение понятного сообщения для пользователя |

## Безопасность и роли пользователей

Приложение использует Spring Security с HTTP Basic Authentication и BCrypt для хеширования паролей.

### Роль USER

**Доступ:**
- Просмотр и управление своими подписками
- Создание пользовательских категорий (CUSTOM)
- Просмотр всех системных категорий (SYSTEM)
- Редактирование только своих CUSTOM категорий
- Удаление только своих CUSTOM категорий
- Просмотр своих уведомлений
- Просмотр своей статистики
- Обновление своего email и пароля

**Ограничения:**
- Не может изменять/удалять SYSTEM категории
- Не может изменять/удалять LEGACY категории
- Не видит других пользователей
- Не может управлять ролями
- Не может удалять пользователей

### Роль ADMIN

**Доступ:**
- Все возможности роли USER
- Просмотр списка всех пользователей
- Удаление пользователей
- Управление ролями пользователей
- Создание и редактирование SYSTEM категорий
- Редактирование и удаление любых CUSTOM категорий (всех пользователей)
- Удаление SYSTEM категорий
- Доступ к расширенной информации в GUI (панель "Пользователи")

**Ограничения:**
- Не может удалять LEGACY категории (никто не может)
- Видит только свои подписки (как и USER)

## Автоматизация и планировщики

Приложение использует Spring `@Scheduled` для автоматической обработки данных:

| Планировщик | Частота выполнения | Назначение |
|-------------|-------------------|------------|
| **NotificationScheduler** | Каждые 60 секунд | Отмечает неотправленные уведомления как отправленные, если дата уведомления уже наступила |
| **PaymentProcessor** | При старте приложения + ежедневно в 00:01 | Обрабатывает просроченные платежи, создает уведомления, обновляет даты следующих списаний |

### NotificationScheduler

**Метод:** `markPendingNotificationsAsSent()`

**Аннотация:** `@Scheduled(fixedRate = 60000)`

**Логика:**
1. Получает все неотправленные уведомления с наступившей датой
2. Отмечает их как отправленные (`isSent = true`)
3. Логирует количество обработанных уведомлений

### PaymentProcessor

**Метод:** `processOverduePayments()`

**Аннотации:**
- `@EventListener(ApplicationReadyEvent.class)` — при старте приложения
- `@Scheduled(cron = "0 1 0 * * *")` — ежедневно в 00:01

**Логика:**
1. Находит все активные подписки с просроченными платежами (`nextBillingDate < today`)
2. Рассчитывает количество пропущенных периодов оплаты
3. Создаёт уведомления `PAYMENT_SUCCESSFUL` за каждый пропущенный период (с проверкой на дубликаты)
4. Обновляет `nextBillingDate` на актуальную дату
5. Логирует количество обработанных платежей и созданных уведомлений

## Графический интерфейс (GUI)

GUI построен на Java Swing с использованием библиотеки **FlatLaf** для современного внешнего вида.

### Панели приложения

| Панель | Доступ | Назначение |
|--------|--------|------------|
| **Подписки** | USER, ADMIN | Просмотр, создание, редактирование, удаление подписок. Фильтрация по категориям и активности |
| **Статистика** | USER, ADMIN | Общая статистика (расходы, количество подписок), топ категорий, предстоящие платежи |
| **Категории** | USER, ADMIN | Управление категориями, создание CUSTOM категорий, просмотр SYSTEM категорий |
| **Уведомления** | USER, ADMIN | Список всех уведомлений с фильтрацией по типу и статусу отправки |
| **Профиль** | USER, ADMIN | Информация о пользователе, изменение email и пароля |
| **Пользователи** | ADMIN | Управление пользователями (просмотр, удаление, изменение ролей) |

### Диалоговые окна

| Диалог | Доступ | Назначение |
|--------|--------|------------|
| `LoginDialog` | Все | Аутентификация (с кнопкой регистрации) |
| `RegistrationDialog` | Все | Регистрация нового пользователя |
| `SubscriptionDialog` | USER, ADMIN | Создание/редактирование подписки |
| `CategoryDialog` | USER, ADMIN | Создание/редактирование категории |
| `UserDialog` | ADMIN | Редактирование пользователя |
| `AboutDialog` | USER, ADMIN | Информация о приложении |

### Отличия для USER и ADMIN

**USER:**
- Видит 5 вкладок (без панели "Пользователи")
- В панели "Категории" может создавать только CUSTOM категории
- Не видит информацию о других пользователях

**ADMIN:**
- Видит 6 вкладок (включая "Пользователи")
- Может удалять пользователей и управлять их ролями
- В панели "Пользователи" отображается список всех пользователей с возможностью удаления и редактирования
- Все остальные панели работают идентично USER (видит только свои подписки)

## Логирование

Приложение использует **Logback** для логирования всех операций.

**Конфигурация:** `src/main/resources/logback-spring.xml`

### Appenders (куда пишутся логи)

| Appender | Назначение | Путь к файлу | Формат даты | Ротация |
|----------|------------|--------------|-------------|---------|
| `CONSOLE` | Вывод в консоль | — | `HH:mm:ss.SSS` | — |
| `FILE` | Все логи приложения | `./logs/application.log` | `yyyy-MM-dd HH:mm:ss` | Ежедневно, 30 дней, макс. 1GB |
| `ERROR_FILE` | Только ошибки (ERROR и выше) | `./logs/error.log` | `yyyy-MM-dd HH:mm:ss` | Ежедневно, 90 дней, макс. 500MB |

**Формат лога:**
```
2025-01-05 12:30:45 [http-nio-8080-exec-1] INFO  c.s.controller.UserController - Created new user: admin
```

### Уровни логирования

| Компонент | Уровень | Что логируется |
|-----------|---------|----------------|
| `com.subscriptionmonitor` | **DEBUG** | Ваш код: все операции (контроллеры, сервисы, планировщики) |
| `org.springframework` | **INFO** | Spring Framework: запуск, конфигурация, важные события |
| `org.hibernate` | **INFO** | Hibernate: создание сессий, транзакции |
| `org.hibernate.SQL` | **DEBUG** | SQL запросы к базе данных |
| `root` (остальное) | **INFO** | Сторонние библиотеки: только важные события |

**Уровни (от меньшего к большему):** DEBUG < INFO < WARN < ERROR

**Что означают уровни:**
- **ERROR** — критические ошибки (приложение сломалось)
- **WARN** — предупреждения (что-то странное, но работает)
- **INFO** — важные события (создан пользователь, запущен сервер)
- **DEBUG** — подробная информация для отладки (SQL запросы, детали операций)

### Что именно логируется

**Контроллеры (REST API):**
- Входящие HTTP запросы (метод, URL, параметры)
- Результаты выполнения операций
- Ошибки валидации и исключения

**Сервисы (бизнес-логика):**
- CRUD операции с сущностями (создание, обновление, удаление)
- Результаты выполнения операций
- Все исключения с полным stack trace

**Планировщики:**
- Запуск планировщиков
- Количество обработанных записей
- Ошибки при обработке

**Безопасность:**
- Попытки аутентификации
- Проверка прав доступа
- Ошибки авторизации (403)

**База данных (SQL):**
- SQL запросы (SELECT, INSERT, UPDATE, DELETE)
- Параметры запросов (bind переменные)
- Транзакции и сессии Hibernate

## Быстрый старт

### 1. Удаление старой БД

```bash
psql -U postgres -c "SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = 'subscription_monitor';"
psql -U postgres -c "DROP DATABASE IF EXISTS subscription_monitor;"
```

### 2. Инициализация БД

```bash
cd src/main/resources/sql
psql -U postgres -f init.sql
```

### 3. Запуск Spring Boot с тестами

```bash
mvn clean test spring-boot:run
```

### 4. Открыть Swagger

[![Swagger UI](https://img.shields.io/badge/Swagger-UI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui.html)

### 5. Запуск GUI

```bash
mvn exec:java
```

**Тестовый пользователь**:

| Username | Password | Role | Email |
|----------|----------|------|-------|
| admin | admin123 | ADMIN | admin@subscriptionmonitor.com |

Дополнительных пользователей можно создать через регистрацию (GUI или API `/api/auth/register`).

## Структура проекта

```
subscription-monitor/
├── src/
│   ├── main/
│   │   ├── java/com/subscriptionmonitor/
│   │   │   ├── config/                       # Настройки приложения
│   │   │   │   ├── DataInitializer.java      # Создание тестовых данных при старте
│   │   │   │   ├── SecurityConfig.java       # Настройки безопасности
│   │   │   │   └── SwaggerConfig.java        # Настройки документации API
│   │   │   ├── controller/                   # Контроллеры REST API
│   │   │   │   ├── AnalyticsController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── NotificationController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── SubscriptionController.java
│   │   │   │   └── UserController.java
│   │   │   ├── dto/                          # Объекты для передачи данных через API
│   │   │   │   ├── CategoryDto.java
│   │   │   │   ├── CategoryResponse.java
│   │   │   │   ├── CategoryStatisticsDto.java
│   │   │   │   ├── ChangePasswordRequest.java
│   │   │   │   ├── CreateCategoryRequest.java
│   │   │   │   ├── CreateNotificationRequest.java
│   │   │   │   ├── CreatePaymentRequest.java
│   │   │   │   ├── CreateSubscriptionRequest.java
│   │   │   │   ├── CreateUserRequest.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── NotificationDto.java
│   │   │   │   ├── NotificationResponse.java
│   │   │   │   ├── PaymentDto.java
│   │   │   │   ├── PaymentResponse.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── SubscriptionDto.java
│   │   │   │   ├── SubscriptionResponse.java
│   │   │   │   ├── UpcomingPaymentDto.java
│   │   │   │   ├── UpdateCategoryRequest.java
│   │   │   │   ├── UpdateEmailRequest.java
│   │   │   │   ├── UpdateNotificationRequest.java
│   │   │   │   ├── UpdatePaymentRequest.java
│   │   │   │   ├── UpdateSubscriptionRequest.java
│   │   │   │   ├── UpdateUserRequest.java
│   │   │   │   ├── UserDto.java
│   │   │   │   ├── UserResponse.java
│   │   │   │   └── UserStatisticsDto.java
│   │   │   ├── exception/                    # Исключения
│   │   │   │   ├── base/                     # Базовые классы
│   │   │   │   │   ├── BaseException.java
│   │   │   │   │   ├── EntityNotFoundException.java
│   │   │   │   │   └── ValidationException.java
│   │   │   │   ├── notfound/                 # Ошибки "не найдено" (404)
│   │   │   │   │   ├── CategoryNotFoundException.java
│   │   │   │   │   ├── NotificationNotFoundException.java
│   │   │   │   │   ├── PaymentNotFoundException.java
│   │   │   │   │   ├── SubscriptionNotFoundException.java
│   │   │   │   │   └── UserNotFoundException.java
│   │   │   │   ├── validation/               # Ошибки валидации (400)
│   │   │   │   │   ├── CategoryValidationException.java
│   │   │   │   │   ├── NotificationValidationException.java
│   │   │   │   │   ├── PaymentValidationException.java
│   │   │   │   │   ├── SubscriptionValidationException.java
│   │   │   │   │   └── UserValidationException.java
│   │   │   │   └── special/                  # Специальные ошибки (410)
│   │   │   │       └── LegacyCategoryException.java
│   │   │   ├── gui/                          # Графический интерфейс (Swing)
│   │   │   │   ├── dialog/                   # Всплывающие окна
│   │   │   │   │   ├── AboutDialog.java
│   │   │   │   │   ├── CategoryDialog.java
│   │   │   │   │   ├── LoginDialog.java
│   │   │   │   │   ├── RegistrationDialog.java
│   │   │   │   │   ├── SubscriptionDialog.java
│   │   │   │   │   └── UserDialog.java
│   │   │   │   ├── exception/                # Обработка ошибок API в GUI
│   │   │   │   │   └── ApiException.java
│   │   │   │   ├── model/                    # Модели таблиц для GUI
│   │   │   │   │   ├── CategoryTableModel.java
│   │   │   │   │   ├── NotificationTableModel.java
│   │   │   │   │   ├── SubscriptionTableModel.java
│   │   │   │   │   └── UserTableModel.java
│   │   │   │   ├── panel/                    # Вкладки приложения
│   │   │   │   │   ├── CategoryPanel.java
│   │   │   │   │   ├── NotificationPanel.java
│   │   │   │   │   ├── ProfilePanel.java
│   │   │   │   │   ├── StatisticsPanel.java
│   │   │   │   │   ├── SubscriptionPanel.java
│   │   │   │   │   └── UserPanel.java
│   │   │   │   ├── util/                     # Вспомогательные классы для GUI
│   │   │   │   │   ├── ErrorDialogUtils.java
│   │   │   │   │   ├── RestClient.java
│   │   │   │   │   ├── StyleUtils.java
│   │   │   │   │   └── ValidationUtils.java
│   │   │   │   └── SubscriptionMonitorGUI.java
│   │   │   ├── handler/                      # Обработка всех ошибок API
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── model/                        # Модели данных
│   │   │   │   ├── entity/                   # Сущности
│   │   │   │   │   ├── BaseEntity.java
│   │   │   │   │   ├── Category.java
│   │   │   │   │   ├── Notification.java
│   │   │   │   │   ├── Payment.java
│   │   │   │   │   ├── Subscription.java
│   │   │   │   │   └── User.java
│   │   │   │   └── enums/                    # Перечисления
│   │   │   │       ├── CategoryType.java
│   │   │   │       ├── Currency.java
│   │   │   │       ├── NotificationType.java
│   │   │   │       └── UserRole.java
│   │   │   ├── repository/                   # Работа с БД
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── NotificationRepository.java
│   │   │   │   ├── PaymentRepository.java
│   │   │   │   ├── SubscriptionRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── scheduler/                    # Автоматические задачи
│   │   │   │   ├── NotificationScheduler.java
│   │   │   │   └── PaymentProcessor.java
│   │   │   ├── security/                     # Безопасность и проверка прав
│   │   │   │   ├── CategorySecurityService.java
│   │   │   │   ├── CustomAccessDeniedHandler.java
│   │   │   │   ├── CustomAuthenticationEntryPoint.java
│   │   │   │   ├── SecurityService.java
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   ├── service/                      # Бизнес-логика
│   │   │   │   ├── AnalyticsService.java
│   │   │   │   ├── CategoryService.java
│   │   │   │   ├── NotificationService.java
│   │   │   │   ├── PaymentService.java
│   │   │   │   ├── SubscriptionService.java
│   │   │   │   └── UserService.java
│   │   │   └── SubscriptionMonitorApplication.java
│   │   └── resources/                        # Конфигурация и скрипты БД
│   │       ├── sql/                          # SQL скрипты для PostgreSQL
│   │       │   ├── init.sql                  # Главный скрипт инициализации
│   │       │   ├── create_database.sql       # Создание БД и расширений
│   │       │   ├── create_tables.sql         # Создание таблиц
│   │       │   ├── constraints.sql           # Constraints (без триггеров)
│   │       │   ├── create_views.sql          # Аналитические витрины (3 view)
│   │       │   ├── create_indexes.sql        # Индексы для оптимизации
│   │       │   ├── create_roles.sql          # Роли БД
│   │       │   ├── clean-database.sql        # Очистка данных БД
│   │       │   └── README.md                 # Документация по БД
│   │       ├── application.properties
│   │       └── logback-spring.xml
│   └── test/java/com/subscriptionmonitor/    # Тесты (5 классов)
│       └── service/                          # Тесты сервисов
│           ├── CategoryServiceTest.java
│           ├── NotificationServiceTest.java
│           ├── PaymentServiceTest.java
│           ├── SubscriptionServiceTest.java
│           └── UserServiceTest.java
├── logs/                                     # Файлы логов
│   ├── application.log
│   └── error.log
├── .gitignore
├── pom.xml
└── README.md
```