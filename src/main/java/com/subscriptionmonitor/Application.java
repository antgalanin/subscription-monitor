package com.subscriptionmonitor;

import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.entity.Category;
import com.subscriptionmonitor.model.entity.Subscription;
import com.subscriptionmonitor.model.enums.UserRole;
import com.subscriptionmonitor.model.enums.Currency;
import com.subscriptionmonitor.model.enums.CategoryType;
import com.subscriptionmonitor.service.UserService;
import com.subscriptionmonitor.service.CategoryService;
import com.subscriptionmonitor.service.SubscriptionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Application {
    private final UserService userService;
    private final CategoryService categoryService;
    private final SubscriptionService subscriptionService;

    private Long adminId;
    private Long user1Id;
    private Long user2Id;
    private Long entertainmentCategoryId;
    private Long productivityCategoryId;
    private Long educationCategoryId;
    private Long financeCategoryId;
    private UUID netflixId;

    public Application() {
        this.userService = new UserService();
        this.categoryService = new CategoryService();
        this.subscriptionService = new SubscriptionService();
    }

    public static void main(String[] args) {
        Application app = new Application();
        app.runDemo();
    }

    public void runDemo() {
        System.out.println("=== Демонстрация работы системы мониторинга подписок ===\n");

        System.out.println("1. Создание пользователей:");
        createUsers();

        System.out.println("\n2. Создание категорий:");
        createCategories();

        System.out.println("\n3. Создание подписок:");
        createSubscriptions();

        System.out.println("\n4. Демонстрация поиска и фильтрации:");
        demonstrateSearch();

        System.out.println("\n5. Демонстрация расчетов:");
        demonstrateCalculations();

        System.out.println("\n6. Демонстрация многопоточной обработки:");
        demonstrateMultithreading();

        System.out.println("\n=== Демонстрация завершена ===");
    }

    private void createUsers() {
        User admin = new User("admin", "admin@example.com", "admin123", UserRole.ADMIN, 5);
        User user1 = new User("ivan_petrov", "ivan@example.com", "password123");
        User user2 = new User("anna_smith", "anna@example.com", "securepass");

        User createdAdmin = userService.create(admin);
        User createdUser1 = userService.create(user1);
        User createdUser2 = userService.create(user2);

        adminId = createdAdmin.getId();
        user1Id = createdUser1.getId();
        user2Id = createdUser2.getId();

        System.out.println("Создано пользователей: " + userService.getTotalCount());
        userService.findAll().forEach(System.out::println);
    }

    private void createCategories() {
        // Системные
        Category entertainment = new Category("Развлечения", CategoryType.SYSTEM, adminId);
        Category productivity = new Category("Продуктивность", CategoryType.SYSTEM, adminId);
        Category education = new Category("Образование", CategoryType.SYSTEM, adminId);
        Category finance = new Category("Финансы", CategoryType.SYSTEM, adminId);

        Category createdEntertainment = categoryService.create(entertainment);
        Category createdProductivity = categoryService.create(productivity);
        Category createdEducation = categoryService.create(education);
        Category createdFinance = categoryService.create(finance);

        entertainmentCategoryId = createdEntertainment.getId();
        productivityCategoryId = createdProductivity.getId();
        educationCategoryId = createdEducation.getId();
        financeCategoryId = createdFinance.getId();

        // Пользователей
        Category gaming = new Category("Игры", CategoryType.CUSTOM, user1Id);
        Category fitness = new Category("Фитнес", CategoryType.CUSTOM, user2Id);

        categoryService.create(gaming);
        categoryService.create(fitness);

        System.out.println("Создано категорий: " + categoryService.getTotalCount());
        System.out.println("Системные категории: " + categoryService.getDefaultCategories().size());
        categoryService.findAll().forEach(System.out::println);
    }

    private void createSubscriptions() {
        Subscription netflix = new Subscription(user1Id, entertainmentCategoryId, "Netflix",
            new BigDecimal("990"), Currency.RUB, 30, LocalDate.now().plusDays(15));
        Subscription spotify = new Subscription(user1Id, entertainmentCategoryId, "Spotify Premium",
            new BigDecimal("399"), Currency.RUB, 30, LocalDate.now().plusDays(3));
        Subscription chatgpt = new Subscription(user1Id, productivityCategoryId, "ChatGPT Plus",
            new BigDecimal("20"), Currency.USD, 30, LocalDate.now().plusDays(10));

        Subscription coursera = new Subscription(user2Id, educationCategoryId, "Coursera Plus",
            new BigDecimal("59"), Currency.USD, 30, LocalDate.now().plusDays(7));
        Subscription notion = new Subscription(user2Id, productivityCategoryId, "Notion Pro",
            new BigDecimal("8"), Currency.USD, 30, LocalDate.now().plusDays(20));

        Subscription createdNetflix = subscriptionService.create(netflix);
        subscriptionService.create(spotify);
        subscriptionService.create(chatgpt);
        subscriptionService.create(coursera);
        subscriptionService.create(notion);

        netflixId = createdNetflix.getUuid();

        System.out.println("Создано подписок: " + subscriptionService.getTotalCount());
        subscriptionService.findAll().forEach(System.out::println);
    }

    private void demonstrateSearch() {
        System.out.println("Поиск пользователя по username:");
        userService.findByUsername("ivan_petrov").ifPresent(System.out::println);

        System.out.println("\nПодписки пользователя ivan_petrov:");
        subscriptionService.getSubscriptionsByUser(user1Id).forEach(System.out::println);

        System.out.println("\nПодписки в валюте USD:");
        subscriptionService.getSubscriptionsByCurrency(Currency.USD).forEach(System.out::println);

        System.out.println("\nПредстоящие списания в течение 5 дней для ivan_petrov:");
        subscriptionService.getUpcomingBillings(user1Id, 5).forEach(System.out::println);

        System.out.println("\nДоступные категории для пользователя anna_smith:");
        categoryService.getAvailableCategoriesForUser(user2Id).forEach(System.out::println);
    }

    private void demonstrateCalculations() {
        System.out.println("Месячные расходы ivan_petrov в рублях: " +
            subscriptionService.calculateTotalMonthlyCost(user1Id, Currency.RUB) + " ₽");

        System.out.println("Месячные расходы ivan_petrov в долларах: $" +
            subscriptionService.calculateTotalMonthlyCost(user1Id, Currency.USD));

        System.out.println("Месячные расходы anna_smith в долларах: $" +
            subscriptionService.calculateTotalMonthlyCost(user2Id, Currency.USD));

        System.out.println("\nДеактивация подписки Netflix...");
        subscriptionService.deactivateSubscription(netflixId);
        System.out.println("Активные подписки ivan_petrov после деактивации:");
        subscriptionService.getActiveSubscriptions(user1Id).forEach(System.out::println);
    }

    private void demonstrateMultithreading() {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Пользователи в разных потоках
        for (int i = 1; i <= 5; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    // Генерируем уникальные имена
                    long threadId = Thread.currentThread().getId();
                    long timestamp = System.nanoTime() % 10000;
                    String username = "user_" + userId + "_" + threadId + "_" + timestamp;
                    String email = "user" + userId + "_" + threadId + "_" + timestamp + "@example.com";
                    User user = new User(username, email, "password" + userId);

                    User created = userService.create(user);
                    System.out.println("Поток " + Thread.currentThread().getName() +
                                     " создал пользователя: " + created.getUsername());
                } catch (Exception e) {
                    System.out.println("Ошибка в потоке " + Thread.currentThread().getName() + ": " + e.getMessage());
                }
            });
        }

        // Подписки в разных потоках
        for (int i = 1; i <= 3; i++) {
            final int subId = i;
            executor.submit(() -> {
                try {
                    long threadId = Thread.currentThread().getId();
                    long timestamp = System.nanoTime() % 10000;
                    String subName = "Service_" + subId + "_" + threadId + "_" + timestamp;
                    Subscription subscription = new Subscription(user1Id, entertainmentCategoryId, subName,
                        new BigDecimal("100").multiply(BigDecimal.valueOf(subId)));

                    Subscription created = subscriptionService.create(subscription);
                    System.out.println("Поток " + Thread.currentThread().getName() +
                                     " создал подписку: " + created.getName());
                } catch (Exception e) {
                    System.out.println("Ошибка в потоке " + Thread.currentThread().getName() + ": " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        System.out.println("\nИтоговая статистика после многопоточной обработки:");
        System.out.println("Всего пользователей: " + userService.getTotalCount());
        System.out.println("Всего подписок: " + subscriptionService.getTotalCount());
    }
}