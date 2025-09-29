package com.subscriptionmonitor.service;

import com.subscriptionmonitor.model.entity.Subscription;
import com.subscriptionmonitor.model.enums.Currency;
import com.subscriptionmonitor.storage.DataStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SubscriptionService Tests")
class SubscriptionServiceTest {

    private SubscriptionService subscriptionService;
    private DataStorage storage;

    @BeforeEach
    void setUp() {
        storage = DataStorage.getInstance();
        storage.clearAll();
        subscriptionService = new SubscriptionService();
    }

    @Test
    @DisplayName("Создание подписки с корректными данными")
    void testCreateSubscription_Success() {
        Long userId = 1L;
        Long categoryId = 2L;
        Subscription subscription = new Subscription(userId, categoryId, "Netflix", new BigDecimal("990"));

        Subscription created = subscriptionService.create(subscription);

        assertNotNull(created);
        assertNotNull(created.getUuid());
        assertEquals("Netflix", created.getName());
        assertEquals(new BigDecimal("990"), created.getCost());
        assertEquals(Currency.RUB, created.getCurrency());
        assertTrue(created.getIsActive());
        assertEquals(1, subscriptionService.getTotalCount());
    }

    @Test
    @DisplayName("Создание подписки с null")
    void testCreateSubscription_Null() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.create(null));

        assertEquals("Subscription cannot be null", exception.getMessage());
        assertEquals(0, subscriptionService.getTotalCount());
    }

    @Test
    @DisplayName("Создание подписки с пустым именем")
    void testCreateSubscription_EmptyName() {
        Long userId = 1L;
        Long categoryId = 2L;
        Subscription subscription = new Subscription(userId, categoryId, "", new BigDecimal("990"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.create(subscription));

        assertTrue(exception.getMessage().contains("name cannot be null or empty"));
        assertEquals(0, subscriptionService.getTotalCount());
    }

    @Test
    @DisplayName("Создание подписки с отрицательной стоимостью")
    void testCreateSubscription_NegativeCost() {
        Long userId = 1L;
        Long categoryId = 2L;
        Subscription subscription = new Subscription(userId, categoryId, "Test", new BigDecimal("-100"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.create(subscription));

        assertTrue(exception.getMessage().contains("Cost cannot be null or negative"));
        assertEquals(0, subscriptionService.getTotalCount());
    }

    @Test
    @DisplayName("Создание подписки с null пользователем")
    void testCreateSubscription_NullUserId() {
        Long categoryId = 2L;
        Subscription subscription = new Subscription(null, categoryId, "Test", new BigDecimal("100"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.create(subscription));

        assertTrue(exception.getMessage().contains("User ID cannot be null"));
        assertEquals(0, subscriptionService.getTotalCount());
    }

    @Test
    @DisplayName("Поиск подписки по ID")
    void testFindById_Success() {
        Long userId = 1L;
        Long categoryId = 2L;
        Subscription subscription = new Subscription(userId, categoryId, "Spotify", new BigDecimal("399"));
        Subscription created = subscriptionService.create(subscription);

        Optional<Subscription> found = subscriptionService.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("Spotify", found.get().getName());
    }

    @Test
    @DisplayName("Поиск подписки по UUID")
    void testFindByUuid_Success() {
        Long userId = 1L;
        Long categoryId = 2L;
        Subscription subscription = new Subscription(userId, categoryId, "Spotify", new BigDecimal("399"));
        Subscription created = subscriptionService.create(subscription);

        Optional<Subscription> found = subscriptionService.findByUuid(created.getUuid());

        assertTrue(found.isPresent());
        assertEquals(created.getUuid(), found.get().getUuid());
        assertEquals("Spotify", found.get().getName());
    }

    @Test
    @DisplayName("Поиск подписки по несуществующему ID")
    void testFindById_NotFound() {
        Optional<Subscription> found = subscriptionService.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Поиск подписки по несуществующему UUID")
    void testFindByUuid_NotFound() {
        Optional<Subscription> found = subscriptionService.findByUuid(UUID.randomUUID());

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Получение всех подписок")
    void testFindAll() {
        Long user1Id = 1L;
        Long user2Id = 2L;
        Long category1Id = 3L;
        Long category2Id = 4L;
        Subscription subscription1 = new Subscription(user1Id, category1Id, "Netflix", new BigDecimal("990"));
        Subscription subscription2 = new Subscription(user2Id, category2Id, "Spotify", new BigDecimal("399"));

        subscriptionService.create(subscription1);
        subscriptionService.create(subscription2);

        List<Subscription> subscriptions = subscriptionService.findAll();

        assertEquals(2, subscriptions.size());
        assertTrue(subscriptions.stream().anyMatch(s -> "Netflix".equals(s.getName())));
        assertTrue(subscriptions.stream().anyMatch(s -> "Spotify".equals(s.getName())));
    }

    @Test
    @DisplayName("Обновление подписки")
    void testUpdateSubscription_Success() {
        Long userId = 1L;
        Long categoryId = 2L;
        Subscription subscription = new Subscription(userId, categoryId, "Netflix", new BigDecimal("990"));
        Subscription created = subscriptionService.create(subscription);

        created.setCost(new BigDecimal("1090"));
        created.setCurrency(Currency.USD);

        Subscription updated = subscriptionService.update(created);

        assertEquals(new BigDecimal("1090"), updated.getCost());
        assertEquals(Currency.USD, updated.getCurrency());
        assertEquals(created.getUuid(), updated.getUuid());
    }

    @Test
    @DisplayName("Обновление несуществующей подписки")
    void testUpdateSubscription_NotFound() {
        Long userId = 1L;
        Long categoryId = 2L;
        Subscription subscription = new Subscription(userId, categoryId, "Test", new BigDecimal("100"));
        subscription.setId(999L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.update(subscription));

        assertTrue(exception.getMessage().contains("Subscription not found"));
    }

    @Test
    @DisplayName("Удаление подписки по ID")
    void testDeleteSubscriptionById_Success() {
        Long userId = 1L;
        Long categoryId = 2L;
        Subscription subscription = new Subscription(userId, categoryId, "Test", new BigDecimal("100"));
        Subscription created = subscriptionService.create(subscription);

        boolean deleted = subscriptionService.deleteById(created.getId());

        assertTrue(deleted);
        assertEquals(0, subscriptionService.getTotalCount());
        assertFalse(subscriptionService.findById(created.getId()).isPresent());
    }

    @Test
    @DisplayName("Удаление подписки по UUID")
    void testDeleteSubscriptionByUuid_Success() {
        Long userId = 1L;
        Long categoryId = 2L;
        Subscription subscription = new Subscription(userId, categoryId, "Test", new BigDecimal("100"));
        Subscription created = subscriptionService.create(subscription);

        boolean deleted = subscriptionService.deleteByUuid(created.getUuid());

        assertTrue(deleted);
        assertEquals(0, subscriptionService.getTotalCount());
        assertFalse(subscriptionService.findByUuid(created.getUuid()).isPresent());
    }

    @Test
    @DisplayName("Получение подписок пользователя")
    void testGetSubscriptionsByUser() {
        Long user1Id = 1L;
        Long user2Id = 2L;
        Long category1Id = 3L;
        Long category2Id = 4L;
        Subscription sub1 = new Subscription(user1Id, category1Id, "Netflix", new BigDecimal("990"));
        Subscription sub2 = new Subscription(user1Id, category2Id, "Spotify", new BigDecimal("399"));
        Subscription sub3 = new Subscription(user2Id, category1Id, "YouTube", new BigDecimal("299"));

        subscriptionService.create(sub1);
        subscriptionService.create(sub2);
        subscriptionService.create(sub3);

        List<Subscription> user1Subs = subscriptionService.getSubscriptionsByUser(user1Id);

        assertEquals(2, user1Subs.size());
        assertTrue(user1Subs.stream().allMatch(s -> s.getUserId().equals(user1Id)));
        assertTrue(user1Subs.stream().anyMatch(s -> "Netflix".equals(s.getName())));
        assertTrue(user1Subs.stream().anyMatch(s -> "Spotify".equals(s.getName())));
    }

    @Test
    @DisplayName("Получение активных подписок пользователя")
    void testGetActiveSubscriptions() {
        Long userId = 1L;
        Long category1Id = 1L;
        Long category2Id = 2L;
        Subscription active1 = new Subscription(userId, category1Id, "Netflix", new BigDecimal("990"));
        Subscription active2 = new Subscription(userId, category2Id, "Spotify", new BigDecimal("399"));
        Subscription inactive = new Subscription(userId, category1Id, "Old Service", new BigDecimal("199"));
        inactive.setIsActive(false);

        subscriptionService.create(active1);
        subscriptionService.create(active2);
        subscriptionService.create(inactive);

        List<Subscription> activeSubs = subscriptionService.getActiveSubscriptions(userId);

        assertEquals(2, activeSubs.size());
        assertTrue(activeSubs.stream().allMatch(s -> s.getIsActive()));
        assertFalse(activeSubs.stream().anyMatch(s -> "Old Service".equals(s.getName())));
    }

    @Test
    @DisplayName("Получение подписок по валюте")
    void testGetSubscriptionsByCurrency() {
        Long user1Id = 1L;
        Long user2Id = 2L;
        Long categoryId = 2L;
        Subscription rub1 = new Subscription(user1Id, categoryId, "Netflix", new BigDecimal("990"));
        rub1.setCurrency(Currency.RUB);
        Subscription rub2 = new Subscription(user2Id, categoryId, "Kinopoisk", new BigDecimal("299"));
        rub2.setCurrency(Currency.RUB);
        Subscription usd1 = new Subscription(user1Id, categoryId, "ChatGPT", new BigDecimal("20"));
        usd1.setCurrency(Currency.USD);

        subscriptionService.create(rub1);
        subscriptionService.create(rub2);
        subscriptionService.create(usd1);

        List<Subscription> rubSubs = subscriptionService.getSubscriptionsByCurrency(Currency.RUB);
        List<Subscription> usdSubs = subscriptionService.getSubscriptionsByCurrency(Currency.USD);

        assertEquals(2, rubSubs.size());
        assertEquals(1, usdSubs.size());
        assertTrue(rubSubs.stream().allMatch(s -> s.getCurrency() == Currency.RUB));
        assertTrue(usdSubs.stream().allMatch(s -> s.getCurrency() == Currency.USD));
    }

    @Test
    @DisplayName("Получение подписок пользователя по валюте")
    void testGetSubscriptionsByUserAndCurrency() {
        Long user1Id = 1L;
        Long user2Id = 2L;
        Long categoryId = 2L;
        Subscription user1_rub = new Subscription(user1Id, categoryId, "Netflix", new BigDecimal("990"));
        user1_rub.setCurrency(Currency.RUB);
        Subscription user1_usd = new Subscription(user1Id, categoryId, "ChatGPT", new BigDecimal("20"));
        user1_usd.setCurrency(Currency.USD);
        Subscription user2_rub = new Subscription(user2Id, categoryId, "Kinopoisk", new BigDecimal("299"));
        user2_rub.setCurrency(Currency.RUB);

        subscriptionService.create(user1_rub);
        subscriptionService.create(user1_usd);
        subscriptionService.create(user2_rub);

        List<Subscription> user1RubSubs = subscriptionService.getSubscriptionsByUserAndCurrency(user1Id, Currency.RUB);

        assertEquals(1, user1RubSubs.size());
        assertEquals("Netflix", user1RubSubs.get(0).getName());
        assertEquals(Currency.RUB, user1RubSubs.get(0).getCurrency());
        assertEquals(user1Id, user1RubSubs.get(0).getUserId());
    }

    @Test
    @DisplayName("Получение предстоящих списаний")
    void testGetUpcomingBillings() {
        Long userId = 1L;
        Long categoryId = 2L;
        Subscription soon = new Subscription(userId, categoryId, "Netflix", new BigDecimal("990"));
        soon.setNextBillingDate(LocalDate.now().plusDays(2));

        Subscription later = new Subscription(userId, categoryId, "Spotify", new BigDecimal("399"));
        later.setNextBillingDate(LocalDate.now().plusDays(10));

        Subscription veryLater = new Subscription(userId, categoryId, "ChatGPT", new BigDecimal("20"));
        veryLater.setNextBillingDate(LocalDate.now().plusDays(20));

        subscriptionService.create(soon);
        subscriptionService.create(later);
        subscriptionService.create(veryLater);

        List<Subscription> upcoming5Days = subscriptionService.getUpcomingBillings(userId, 5);
        List<Subscription> upcoming15Days = subscriptionService.getUpcomingBillings(userId, 15);

        assertEquals(1, upcoming5Days.size());
        assertEquals("Netflix", upcoming5Days.get(0).getName());

        assertEquals(2, upcoming15Days.size());
        assertTrue(upcoming15Days.stream().anyMatch(s -> "Netflix".equals(s.getName())));
        assertTrue(upcoming15Days.stream().anyMatch(s -> "Spotify".equals(s.getName())));
    }

    @Test
    @DisplayName("Деактивация подписки")
    void testDeactivateSubscription() {
        Long userId = 1L;
        Long categoryId = 2L;
        Subscription subscription = new Subscription(userId, categoryId, "Netflix", new BigDecimal("990"));
        Subscription created = subscriptionService.create(subscription);

        boolean result = subscriptionService.deactivateSubscription(created.getUuid());

        assertTrue(result);
        Optional<Subscription> updated = subscriptionService.findByUuid(created.getUuid());
        assertTrue(updated.isPresent());
        assertFalse(updated.get().getIsActive());
    }

    @Test
    @DisplayName("Активация подписки")
    void testActivateSubscription() {
        Long userId = 1L;
        Long categoryId = 2L;
        Subscription subscription = new Subscription(userId, categoryId, "Netflix", new BigDecimal("990"));
        subscription.setIsActive(false);
        Subscription created = subscriptionService.create(subscription);

        boolean result = subscriptionService.activateSubscription(created.getUuid());

        assertTrue(result);
        Optional<Subscription> updated = subscriptionService.findByUuid(created.getUuid());
        assertTrue(updated.isPresent());
        assertTrue(updated.get().getIsActive());
    }

    @Test
    @DisplayName("Расчет месячной стоимости подписок")
    void testCalculateTotalMonthlyCost() {
        Long userId = 1L;
        Long categoryId = 2L;
        Subscription monthly = new Subscription(userId, categoryId, "Netflix", new BigDecimal("990"));
        monthly.setBillingPeriodDays(30);
        monthly.setCurrency(Currency.RUB);

        Subscription weekly = new Subscription(userId, categoryId, "Weekly Service", new BigDecimal("100"));
        weekly.setBillingPeriodDays(7);
        weekly.setCurrency(Currency.RUB);

        subscriptionService.create(monthly);
        subscriptionService.create(weekly);

        BigDecimal totalMonthlyCost = subscriptionService.calculateTotalMonthlyCost(userId, Currency.RUB);

        // 990 (monthly) + 100 * 30 / 7 (weekly converted to monthly) ≈ 990 + 428.57 = 1418.57
        BigDecimal expectedWeeklyCost = new BigDecimal("100").multiply(BigDecimal.valueOf(30))
                .divide(BigDecimal.valueOf(7), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal expected = new BigDecimal("990").add(expectedWeeklyCost);

        assertEquals(expected, totalMonthlyCost);
    }

    @Test
    @DisplayName("Расчет месячной стоимости для пустого списка")
    void testCalculateTotalMonthlyCost_Empty() {
        Long userId = 1L;
        BigDecimal totalMonthlyCost = subscriptionService.calculateTotalMonthlyCost(userId, Currency.RUB);

        assertEquals(BigDecimal.ZERO, totalMonthlyCost);
    }

    @Test
    @DisplayName("Расчет месячной стоимости с null параметрами")
    void testCalculateTotalMonthlyCost_NullParams() {
        Long userId = 1L;
        BigDecimal result1 = subscriptionService.calculateTotalMonthlyCost(null, Currency.RUB);
        BigDecimal result2 = subscriptionService.calculateTotalMonthlyCost(userId, null);

        assertEquals(BigDecimal.ZERO, result1);
        assertEquals(BigDecimal.ZERO, result2);
    }
}