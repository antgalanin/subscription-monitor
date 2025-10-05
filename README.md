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
- Поля: `userId`, `subscriptionId`, `notificationDate`, `type` (UPCOMING_PAYMENT), `isSent`, `message`

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
