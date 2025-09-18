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
        Subscription subscription = new Subscription(1L, 1L, "Netflix", new BigDecimal("990"));

        Subscription created = subscriptionService.create(subscription);

        assertNotNull(created);
        assertNotNull(created.getId());
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
        Subscription subscription = new Subscription(1L, 1L, "", new BigDecimal("990"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.create(subscription));

        assertTrue(exception.getMessage().contains("name cannot be null or empty"));
        assertEquals(0, subscriptionService.getTotalCount());
    }

    @Test
    @DisplayName("Создание подписки с отрицательной стоимостью")
    void testCreateSubscription_NegativeCost() {
        Subscription subscription = new Subscription(1L, 1L, "Test", new BigDecimal("-100"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.create(subscription));

        assertTrue(exception.getMessage().contains("Cost cannot be null or negative"));
        assertEquals(0, subscriptionService.getTotalCount());
    }

    @Test
    @DisplayName("Создание подписки с null пользователем")
    void testCreateSubscription_NullUserId() {
        Subscription subscription = new Subscription(null, 1L, "Test", new BigDecimal("100"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.create(subscription));

        assertTrue(exception.getMessage().contains("User ID cannot be null"));
        assertEquals(0, subscriptionService.getTotalCount());
    }

    @Test
    @DisplayName("Поиск подписки по ID")
    void testFindById_Success() {
        Subscription subscription = new Subscription(1L, 1L, "Spotify", new BigDecimal("399"));
        Subscription created = subscriptionService.create(subscription);

        Optional<Subscription> found = subscriptionService.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("Spotify", found.get().getName());
    }

    @Test
    @DisplayName("Поиск подписки по несуществующему ID")
    void testFindById_NotFound() {
        Optional<Subscription> found = subscriptionService.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Получение всех подписок")
    void testFindAll() {
        Subscription subscription1 = new Subscription(1L, 1L, "Netflix", new BigDecimal("990"));
        Subscription subscription2 = new Subscription(2L, 2L, "Spotify", new BigDecimal("399"));

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
        Subscription subscription = new Subscription(1L, 1L, "Netflix", new BigDecimal("990"));
        Subscription created = subscriptionService.create(subscription);

        created.setCost(new BigDecimal("1090"));
        created.setCurrency(Currency.USD);

        Subscription updated = subscriptionService.update(created);

        assertEquals(new BigDecimal("1090"), updated.getCost());
        assertEquals(Currency.USD, updated.getCurrency());
        assertEquals(created.getId(), updated.getId());
    }

    @Test
    @DisplayName("Обновление несуществующей подписки")
    void testUpdateSubscription_NotFound() {
        Subscription subscription = new Subscription(1L, 1L, "Test", new BigDecimal("100"));
        subscription.setId(999L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.update(subscription));

        assertTrue(exception.getMessage().contains("Subscription not found"));
    }

    @Test
    @DisplayName("Удаление подписки")
    void testDeleteSubscription_Success() {
        Subscription subscription = new Subscription(1L, 1L, "Test", new BigDecimal("100"));
        Subscription created = subscriptionService.create(subscription);

        boolean deleted = subscriptionService.deleteById(created.getId());

        assertTrue(deleted);
        assertEquals(0, subscriptionService.getTotalCount());
        assertFalse(subscriptionService.findById(created.getId()).isPresent());
    }

    @Test
    @DisplayName("Получение подписок пользователя")
    void testGetSubscriptionsByUser() {
        Subscription sub1 = new Subscription(1L, 1L, "Netflix", new BigDecimal("990"));
        Subscription sub2 = new Subscription(1L, 2L, "Spotify", new BigDecimal("399"));
        Subscription sub3 = new Subscription(2L, 1L, "YouTube", new BigDecimal("299"));

        subscriptionService.create(sub1);
        subscriptionService.create(sub2);
        subscriptionService.create(sub3);

        List<Subscription> user1Subs = subscriptionService.getSubscriptionsByUser(1L);

        assertEquals(2, user1Subs.size());
        assertTrue(user1Subs.stream().allMatch(s -> s.getUserId().equals(1L)));
        assertTrue(user1Subs.stream().anyMatch(s -> "Netflix".equals(s.getName())));
        assertTrue(user1Subs.stream().anyMatch(s -> "Spotify".equals(s.getName())));
    }

    @Test
    @DisplayName("Получение активных подписок пользователя")
    void testGetActiveSubscriptions() {
        Subscription active1 = new Subscription(1L, 1L, "Netflix", new BigDecimal("990"));
        Subscription active2 = new Subscription(1L, 2L, "Spotify", new BigDecimal("399"));
        Subscription inactive = new Subscription(1L, 1L, "Old Service", new BigDecimal("199"));
        inactive.setIsActive(false);

        subscriptionService.create(active1);
        subscriptionService.create(active2);
        subscriptionService.create(inactive);

        List<Subscription> activeSubs = subscriptionService.getActiveSubscriptions(1L);

        assertEquals(2, activeSubs.size());
        assertTrue(activeSubs.stream().allMatch(s -> s.getIsActive()));
        assertFalse(activeSubs.stream().anyMatch(s -> "Old Service".equals(s.getName())));
    }

    @Test
    @DisplayName("Получение подписок по валюте")
    void testGetSubscriptionsByCurrency() {
        Subscription rub1 = new Subscription(1L, 1L, "Netflix", new BigDecimal("990"));
        rub1.setCurrency(Currency.RUB);
        Subscription rub2 = new Subscription(2L, 1L, "Kinopoisk", new BigDecimal("299"));
        rub2.setCurrency(Currency.RUB);
        Subscription usd1 = new Subscription(1L, 1L, "ChatGPT", new BigDecimal("20"));
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
        Subscription user1_rub = new Subscription(1L, 1L, "Netflix", new BigDecimal("990"));
        user1_rub.setCurrency(Currency.RUB);
        Subscription user1_usd = new Subscription(1L, 1L, "ChatGPT", new BigDecimal("20"));
        user1_usd.setCurrency(Currency.USD);
        Subscription user2_rub = new Subscription(2L, 1L, "Kinopoisk", new BigDecimal("299"));
        user2_rub.setCurrency(Currency.RUB);

        subscriptionService.create(user1_rub);
        subscriptionService.create(user1_usd);
        subscriptionService.create(user2_rub);

        List<Subscription> user1RubSubs = subscriptionService.getSubscriptionsByUserAndCurrency(1L, Currency.RUB);

        assertEquals(1, user1RubSubs.size());
        assertEquals("Netflix", user1RubSubs.get(0).getName());
        assertEquals(Currency.RUB, user1RubSubs.get(0).getCurrency());
        assertEquals(1L, user1RubSubs.get(0).getUserId());
    }

    @Test
    @DisplayName("Получение предстоящих списаний")
    void testGetUpcomingBillings() {
        Subscription soon = new Subscription(1L, 1L, "Netflix", new BigDecimal("990"));
        soon.setNextBillingDate(LocalDate.now().plusDays(2));

        Subscription later = new Subscription(1L, 1L, "Spotify", new BigDecimal("399"));
        later.setNextBillingDate(LocalDate.now().plusDays(10));

        Subscription veryLater = new Subscription(1L, 1L, "ChatGPT", new BigDecimal("20"));
        veryLater.setNextBillingDate(LocalDate.now().plusDays(20));

        subscriptionService.create(soon);
        subscriptionService.create(later);
        subscriptionService.create(veryLater);

        List<Subscription> upcoming5Days = subscriptionService.getUpcomingBillings(1L, 5);
        List<Subscription> upcoming15Days = subscriptionService.getUpcomingBillings(1L, 15);

        assertEquals(1, upcoming5Days.size());
        assertEquals("Netflix", upcoming5Days.get(0).getName());

        assertEquals(2, upcoming15Days.size());
        assertTrue(upcoming15Days.stream().anyMatch(s -> "Netflix".equals(s.getName())));
        assertTrue(upcoming15Days.stream().anyMatch(s -> "Spotify".equals(s.getName())));
    }

    @Test
    @DisplayName("Деактивация подписки")
    void testDeactivateSubscription() {
        Subscription subscription = new Subscription(1L, 1L, "Netflix", new BigDecimal("990"));
        Subscription created = subscriptionService.create(subscription);

        boolean result = subscriptionService.deactivateSubscription(created.getId());

        assertTrue(result);
        Optional<Subscription> updated = subscriptionService.findById(created.getId());
        assertTrue(updated.isPresent());
        assertFalse(updated.get().getIsActive());
    }

    @Test
    @DisplayName("Активация подписки")
    void testActivateSubscription() {
        Subscription subscription = new Subscription(1L, 1L, "Netflix", new BigDecimal("990"));
        subscription.setIsActive(false);
        Subscription created = subscriptionService.create(subscription);

        boolean result = subscriptionService.activateSubscription(created.getId());

        assertTrue(result);
        Optional<Subscription> updated = subscriptionService.findById(created.getId());
        assertTrue(updated.isPresent());
        assertTrue(updated.get().getIsActive());
    }

    @Test
    @DisplayName("Расчет месячной стоимости подписок")
    void testCalculateTotalMonthlyCost() {
        Subscription monthly = new Subscription(1L, 1L, "Netflix", new BigDecimal("990"));
        monthly.setBillingPeriodDays(30);
        monthly.setCurrency(Currency.RUB);

        Subscription weekly = new Subscription(1L, 1L, "Weekly Service", new BigDecimal("100"));
        weekly.setBillingPeriodDays(7);
        weekly.setCurrency(Currency.RUB);

        subscriptionService.create(monthly);
        subscriptionService.create(weekly);

        BigDecimal totalMonthlyCost = subscriptionService.calculateTotalMonthlyCost(1L, Currency.RUB);

        // 990 (monthly) + 100 * 30 / 7 (weekly converted to monthly) ≈ 990 + 428.57 = 1418.57
        BigDecimal expectedWeeklyCost = new BigDecimal("100").multiply(BigDecimal.valueOf(30))
                .divide(BigDecimal.valueOf(7), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal expected = new BigDecimal("990").add(expectedWeeklyCost);

        assertEquals(expected, totalMonthlyCost);
    }

    @Test
    @DisplayName("Расчет месячной стоимости для пустого списка")
    void testCalculateTotalMonthlyCost_Empty() {
        BigDecimal totalMonthlyCost = subscriptionService.calculateTotalMonthlyCost(1L, Currency.RUB);

        assertEquals(BigDecimal.ZERO, totalMonthlyCost);
    }

    @Test
    @DisplayName("Расчет месячной стоимости с null параметрами")
    void testCalculateTotalMonthlyCost_NullParams() {
        BigDecimal result1 = subscriptionService.calculateTotalMonthlyCost(null, Currency.RUB);
        BigDecimal result2 = subscriptionService.calculateTotalMonthlyCost(1L, null);

        assertEquals(BigDecimal.ZERO, result1);
        assertEquals(BigDecimal.ZERO, result2);
    }
}