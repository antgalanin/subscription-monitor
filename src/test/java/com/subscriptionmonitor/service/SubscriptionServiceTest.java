package com.subscriptionmonitor.service;

import com.subscriptionmonitor.model.entity.Subscription;
import com.subscriptionmonitor.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService Tests")
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private Subscription testSubscription;
    private UUID testSubscriptionId;
    private UUID userId;
    private UUID category1Id;
    private UUID category2Id;
    private UUID subscription1Id;
    private UUID subscription2Id;

    @BeforeEach
    void setUp() {
        testSubscriptionId = UUID.randomUUID();
        userId = UUID.randomUUID();
        category1Id = UUID.randomUUID();
        category2Id = UUID.randomUUID();
        subscription1Id = UUID.randomUUID();
        subscription2Id = UUID.randomUUID();

        testSubscription = new Subscription(userId, category2Id, "Netflix", new BigDecimal("990.00"));
        testSubscription.setId(testSubscriptionId);
    }

    @Test
    @DisplayName("Создание подписки с корректными данными")
    void testCreateSubscription_Success() {
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        Subscription created = subscriptionService.create(testSubscription);

        assertNotNull(created);
        assertEquals(testSubscriptionId, created.getId());
        assertEquals("Netflix", created.getName());
        assertEquals(userId, created.getUserId());
        assertEquals(category2Id, created.getCategoryId());
        assertTrue(created.getIsActive());

        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Поиск подписки по ID")
    void testFindById_Success() {
        when(subscriptionRepository.findById(testSubscriptionId)).thenReturn(Optional.of(testSubscription));

        Optional<Subscription> found = subscriptionService.findById(testSubscriptionId);

        assertTrue(found.isPresent());
        assertEquals(testSubscriptionId, found.get().getId());
        assertEquals("Netflix", found.get().getName());

        verify(subscriptionRepository, times(1)).findById(testSubscriptionId);
    }

    @Test
    @DisplayName("Поиск подписки по несуществующему ID")
    void testFindById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(subscriptionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<Subscription> found = subscriptionService.findById(nonExistentId);

        assertFalse(found.isPresent());

        verify(subscriptionRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Получение всех подписок")
    void testFindAll() {
        Subscription subscription1 = new Subscription(userId, category1Id, "Netflix", new BigDecimal("990.00"));
        subscription1.setId(subscription1Id);
        Subscription subscription2 = new Subscription(userId, category2Id, "Spotify", new BigDecimal("399.00"));
        subscription2.setId(subscription2Id);

        when(subscriptionRepository.findAll()).thenReturn(Arrays.asList(subscription1, subscription2));

        List<Subscription> subscriptions = subscriptionService.findAll();

        assertEquals(2, subscriptions.size());
        assertTrue(subscriptions.stream().anyMatch(s -> "Netflix".equals(s.getName())));
        assertTrue(subscriptions.stream().anyMatch(s -> "Spotify".equals(s.getName())));

        verify(subscriptionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Поиск подписок по пользователю")
    void testFindByUserId() {
        Subscription subscription1 = new Subscription(userId, category1Id, "Netflix", new BigDecimal("990.00"));
        subscription1.setId(subscription1Id);
        Subscription subscription2 = new Subscription(userId, category2Id, "Spotify", new BigDecimal("399.00"));
        subscription2.setId(subscription2Id);

        when(subscriptionRepository.findByUserId(userId))
                .thenReturn(Arrays.asList(subscription1, subscription2));

        List<Subscription> subscriptions = subscriptionService.findByUserId(userId);

        assertEquals(2, subscriptions.size());
        assertTrue(subscriptions.stream().allMatch(s -> s.getUserId().equals(userId)));

        verify(subscriptionRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Поиск подписок по пользователю и статусу активности")
    void testFindByUserIdAndIsActive() {
        Subscription subscription1 = new Subscription(userId, category1Id, "Netflix", new BigDecimal("990.00"));
        subscription1.setId(subscription1Id);
        subscription1.setIsActive(true);

        when(subscriptionRepository.findByUserIdAndIsActive(userId, true))
                .thenReturn(Arrays.asList(subscription1));

        List<Subscription> activeSubscriptions = subscriptionService.findByUserIdAndIsActive(userId, true);

        assertEquals(1, activeSubscriptions.size());
        assertTrue(activeSubscriptions.get(0).getIsActive());
        assertEquals(userId, activeSubscriptions.get(0).getUserId());

        verify(subscriptionRepository, times(1)).findByUserIdAndIsActive(userId, true);
    }

    @Test
    @DisplayName("Поиск активных подписок пользователя")
    void testFindActiveSubscriptionsByUserId() {
        Subscription subscription1 = new Subscription(userId, category1Id, "Netflix", new BigDecimal("990.00"));
        subscription1.setId(subscription1Id);
        subscription1.setIsActive(true);

        when(subscriptionRepository.findActiveSubscriptionsByUserId(userId))
                .thenReturn(Arrays.asList(subscription1));

        List<Subscription> activeSubscriptions = subscriptionService.findActiveSubscriptionsByUserId(userId);

        assertEquals(1, activeSubscriptions.size());
        assertTrue(activeSubscriptions.get(0).getIsActive());

        verify(subscriptionRepository, times(1)).findActiveSubscriptionsByUserId(userId);
    }

    @Test
    @DisplayName("Поиск подписок по категории")
    void testFindByCategoryId() {
        UUID user2Id = UUID.randomUUID();
        Subscription subscription1 = new Subscription(userId, category1Id, "Netflix", new BigDecimal("990.00"));
        subscription1.setId(subscription1Id);
        Subscription subscription2 = new Subscription(user2Id, category1Id, "Amazon Prime", new BigDecimal("599.00"));
        subscription2.setId(subscription2Id);

        when(subscriptionRepository.findByCategoryId(category1Id))
                .thenReturn(Arrays.asList(subscription1, subscription2));

        List<Subscription> subscriptions = subscriptionService.findByCategoryId(category1Id);

        assertEquals(2, subscriptions.size());
        assertTrue(subscriptions.stream().allMatch(s -> s.getCategoryId().equals(category1Id)));

        verify(subscriptionRepository, times(1)).findByCategoryId(category1Id);
    }

    @Test
    @DisplayName("Обновление подписки")
    void testUpdateSubscription_Success() {
        Subscription updatedSubscription = new Subscription(userId, category2Id, "Netflix Premium", new BigDecimal("1290.00"));
        updatedSubscription.setId(testSubscriptionId);

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(updatedSubscription);

        Subscription updated = subscriptionService.update(updatedSubscription);

        assertEquals("Netflix Premium", updated.getName());
        assertEquals(new BigDecimal("1290.00"), updated.getCost());
        assertEquals(testSubscriptionId, updated.getId());

        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Удаление подписки")
    void testDeleteSubscription_Success() {
        doNothing().when(subscriptionRepository).deleteById(testSubscriptionId);

        subscriptionService.delete(testSubscriptionId);

        verify(subscriptionRepository, times(1)).deleteById(testSubscriptionId);
    }

    @Test
    @DisplayName("Деактивация подписки")
    void testDeactivateSubscription_Success() {
        Subscription activeSubscription = new Subscription(userId, category1Id, "Netflix", new BigDecimal("990.00"));
        activeSubscription.setId(testSubscriptionId);
        activeSubscription.setIsActive(true);

        when(subscriptionRepository.findById(testSubscriptionId)).thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Subscription> result = subscriptionService.deactivate(testSubscriptionId);

        assertTrue(result.isPresent());
        assertFalse(result.get().getIsActive());
        assertEquals(testSubscriptionId, result.get().getId());
        verify(subscriptionRepository, times(1)).findById(testSubscriptionId);
        verify(subscriptionRepository, times(1)).save(activeSubscription);
    }

    @Test
    @DisplayName("Удаление всех подписок")
    void testDeleteAll() {
        doNothing().when(subscriptionRepository).deleteAll();

        subscriptionService.deleteAll();

        verify(subscriptionRepository, times(1)).deleteAll();
    }
}
