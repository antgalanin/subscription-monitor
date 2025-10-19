# Система мониторинга подписок

## Идея проекта

Система для отслеживания персональных подписок пользователей.
Помогает контролировать расходы и напоминает о предстоящих списаниях денег.

## Что я хотел реализовать

Создать систему, которая может:
- Хранить информацию о пользователях и их подписках
- Группировать подписки по категориям
- Считать общие траты по месяцам
- Показывать, какие подписки скоро спишут деньги
- Работать с несколькими пользователями одновременно

---

## Лабораторная работа №1 "Базовые типы данных"

## Основные классы и их назначение

### Модели данных

**BaseEntity** - базовый класс для всех сущностей
- Хранит общие поля: `id`, `uuid`, `createdAt`
- UUID генерируется автоматически для надежности
- Реализует `equals()`, `hashCode()`, `toString()`

**User** - пользователь системы
- Поля: `username`, `email`, `password`, `role` (USER/ADMIN), `notificationDays`
- Может быть обычным пользователем или администратором

**Category** - категория подписок
- Поля: `name`, `type` (CategoryType), `createdByUserId`
- Типы: SYSTEM (системные), CUSTOM (пользовательские), LEGACY (устаревшие)

**Payment** - платежная информация
- Поля: `cost`, `currency`, `billingPeriodDays`, `nextBillingDate`
- Выделен в отдельную сущность для лучшей архитектуры

**Subscription** - подписка пользователя
- Поля: `userId`, `categoryId`, `name`, `payment` (композиция), `isActive`
- Использует Payment для хранения финансовой информации
- Convenience методы для обратной совместимости

### Сервисы

**CrudService** - интерфейс с базовыми операциями
- Методы: `create()`, `findById()`, `findAll()`, `update()`, `deleteById()`

**UserService** - работа с пользователями
- Проверяет уникальность username и email
- Поиск по имени, email, роли

**PaymentService** - работа с платежами
- Поиск и фильтрация по валюте
- Расчет общих сумм по валютам

**CategoryService** - работа с категориями
- Работает с CategoryType enum (SYSTEM/CUSTOM/LEGACY)
- Показывает доступные категории для конкретного пользователя

**SubscriptionService** - работа с подписками
- Считает месячные расходы по валютам
- Находит подписки с предстоящими списаниями
- Может активировать/деактивировать подписки

### Хранение данных

**DataStorage** - singleton для хранения всех данных в памяти
- Использует `ConcurrentHashMap` для безопасной работы в многопоточности
- Генерирует уникальные ID через `AtomicLong`

## Демонстрация работы

**Application.java**:
1. Создание пользователей и администратора
2. Создание системных и пользовательских категорий
3. Добавление подписок с разными валютами и периодами
4. Поиск и фильтрацию данных
5. Расчет месячных трат
6. Многопоточное добавление данных

---

## Лабораторная работа №2 "Создание собственных классов"

### Модели данных

**BaseEntity** - базовый класс для всех сущностей
- Поля: `id` (UUID), `createdAt` (LocalDateTime)
- UUID генерируется автоматически через @PrePersist

**User** - пользователи системы
- Поля: `username`, `email`, `password`, `role` (USER/ADMIN), `notificationDays`
- Уникальные constraints на username и email

**Category** - категории подписок
- Поля: `name`, `type` (SYSTEM/CUSTOM), `createdByUserId`
- Системные и пользовательские категории

**Payment** - платежная информация
- Поля: `cost` (BigDecimal), `currency` (RUB/USD/EUR), `billingPeriodDays`, `nextBillingDate`


**Subscription** - подписки пользователей
- Поля: `userId`, `categoryId`, `name`, `payment` (@ManyToOne), `isActive`
- Связь @ManyToOne с Payment

**Notification** (НОВАЯ) - уведомления о списаниях
- Поля: `userId`, `subscriptionId`, `notificationDate`, `type` (UPCOMING_PAYMENT, PAYMENT_SUCCESSFUL), `isSent`, `message`

### REST API

Архитектура: `Controller → Service → Repository → PostgreSQL`

**User API** (`/api/users`)
- CRUD операции
- Фильтрация по роли (USER/ADMIN)

**Category API** (`/api/categories`)
- CRUD операции
- Фильтрация по типу и создателю

**Payment API** (`/api/payments`)
- CRUD операции
- Фильтрация по валюте и дате списания

**Subscription API** (`/api/subscriptions`)
- CRUD операции
- Фильтрация по пользователю, категории, активности

**Notification API** (`/api/notifications`)
- CRUD операции
- Получение pending уведомлений
- Отметка как отправленные

---

## Лабораторная работа №3 "Обработка исключений"

### Иерархия исключений

**BaseException** - базовое исключение для всей системы
- Поля: `code` (код ошибки), `args` (параметры для сообщения)
- Наследуется от Exception

**EntityNotFoundException** - базовое исключение для "сущность не найдена"
- Наследуется от BaseException
- HTTP статус: 404 NOT_FOUND

**ValidationException** - базовое исключение для ошибок валидации
- Наследуется от BaseException
- HTTP статус: 400 BAD_REQUEST

### Специфические исключения

**NotFound исключения** (`exception/notfound`) - для всех 5 моделей:
- `UserNotFoundException` - пользователь не найден
- `CategoryNotFoundException` - категория не найдена
- `PaymentNotFoundException` - платеж не найден
- `SubscriptionNotFoundException` - подписка не найдена
- `NotificationNotFoundException` - уведомление не найдено

**Validation исключения** (`exception/validation`) - для всех 5 моделей:
- `UserValidationException` - ошибки валидации пользователя
- `CategoryValidationException` - ошибки валидации категории
- `PaymentValidationException` - ошибки валидации платежа
- `SubscriptionValidationException` - ошибки валидации подписки
- `NotificationValidationException` - ошибки валидации уведомления

**Специальные исключения** (`exception/special`):
- `LegacyCategoryException` - попытка использования устаревшей категории (HTTP 410 GONE)
- `AccessDeniedException` - отказ в доступе (HTTP 403 FORBIDDEN) (lab4 - Spring Security)

### Обработка исключений

**GlobalExceptionHandler** - централизованная обработка всех исключений
- `@RestControllerAdvice` для перехвата исключений во всех контроллерах
- Обработчики для каждого типа исключений

**ErrorResponse** - DTO для ответов об ошибках
- Поля: `status`, `error`, `message`, `code`, `timestamp`, `path`
- JSON-форматирование timestamp

---

## Лабораторная работа №4 "Spring Security и логирование"

### Spring Security

**Конфигурация безопасности:**
- HTTP Basic Authentication - каждый запрос содержит username и
  password
- Без сохранения сессий на сервере (каждый запрос проверяется
  заново)
- BCryptPasswordEncoder - пароли хранятся в зашифрованном виде
- CSRF защита отключена (не нужна для REST API)

**Роли:**
- ADMIN - полный доступ
- USER - доступ только к своим данным

**Ownership проверки:**
- `SecurityService.isOwner()` - проверяет владение ресурсом
- `@PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")` - на уровне методов

**Custom обработчики:**
- `CustomAuthenticationEntryPoint` - обработка 401 Unauthorized
- `CustomAccessDeniedHandler` - обработка 403 Forbidden

### Логирование (Logback)

**Конфигурация** (`logback-spring.xml`):
- **CONSOLE appender** - вывод в терминал (HH:mm:ss.SSS)
- **FILE appender** - все логи в `logs/application.log` (30 дней, 1GB)
- **ERROR_FILE appender** - только ошибки в `logs/error.log` (90 дней, 500MB)

**Уровни логирования:**
- `DEBUG` - детальная информация (пакет `com.subscriptionmonitor`)
- `INFO` - стандартные события (Spring, Hibernate, запуск/остановка)
- `WARN` - предупреждения (deprecated методы, некритичные проблемы)
- `ERROR` - критичные ошибки (исключения, security нарушения)

**Что логируется:**
- Запуск и остановка приложения
- Создание, изменение и удаление данных
- Попытки неавторизованного доступа
- Повторяющиеся username или email при регистрации
- Полное описание ошибок для их устранения