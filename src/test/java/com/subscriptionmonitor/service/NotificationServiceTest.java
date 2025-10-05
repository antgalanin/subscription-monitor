package com.subscriptionmonitor.service;

import com.subscriptionmonitor.model.entity.Notification;
import com.subscriptionmonitor.model.enums.NotificationType;
import com.subscriptionmonitor.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;
    private UUID testNotificationId;
    private UUID userId;
    private UUID subscription1Id;
    private UUID subscription2Id;
    private UUID notification1Id;
    private UUID notification2Id;

    @BeforeEach
    void setUp() {
        testNotificationId = UUID.randomUUID();
        userId = UUID.randomUUID();
        subscription1Id = UUID.randomUUID();
        subscription2Id = UUID.randomUUID();
        notification1Id = UUID.randomUUID();
        notification2Id = UUID.randomUUID();

        testNotification = new Notification(
                userId,
                subscription1Id,
                LocalDateTime.now().plusDays(1),
                NotificationType.UPCOMING_PAYMENT,
                "Your subscription will renew in 1 day"
        );
        testNotification.setId(testNotificationId);
    }

    @Test
    @DisplayName("Создание уведомления с корректными данными")
    void testCreateNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        Notification created = notificationService.create(testNotification);

        assertNotNull(created);
        assertEquals(testNotificationId, created.getId());
        assertEquals(userId, created.getUserId());
        assertEquals(subscription1Id, created.getSubscriptionId());
        assertEquals(NotificationType.UPCOMING_PAYMENT, created.getType());
        assertFalse(created.getIsSent());

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Поиск уведомления по ID")
    void testFindById_Success() {
        when(notificationRepository.findById(testNotificationId)).thenReturn(Optional.of(testNotification));

        Optional<Notification> found = notificationService.findById(testNotificationId);

        assertTrue(found.isPresent());
        assertEquals(testNotificationId, found.get().getId());
        assertEquals(NotificationType.UPCOMING_PAYMENT, found.get().getType());

        verify(notificationRepository, times(1)).findById(testNotificationId);
    }

    @Test
    @DisplayName("Поиск уведомления по несуществующему ID")
    void testFindById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(notificationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<Notification> found = notificationService.findById(nonExistentId);

        assertFalse(found.isPresent());

        verify(notificationRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Получение всех уведомлений")
    void testFindAll() {
        Notification notification1 = new Notification(
                userId, subscription1Id, LocalDateTime.now(), NotificationType.UPCOMING_PAYMENT, "Message 1"
        );
        notification1.setId(notification1Id);
        Notification notification2 = new Notification(
                userId, subscription2Id, LocalDateTime.now(), NotificationType.UPCOMING_PAYMENT, "Message 2"
        );
        notification2.setId(notification2Id);

        when(notificationRepository.findAll()).thenReturn(Arrays.asList(notification1, notification2));

        List<Notification> notifications = notificationService.findAll();

        assertEquals(2, notifications.size());

        verify(notificationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Поиск уведомлений по пользователю")
    void testFindByUserId() {
        Notification notification1 = new Notification(
                userId, subscription1Id, LocalDateTime.now(), NotificationType.UPCOMING_PAYMENT, "Message 1"
        );
        notification1.setId(notification1Id);
        Notification notification2 = new Notification(
                userId, subscription2Id, LocalDateTime.now(), NotificationType.UPCOMING_PAYMENT, "Message 2"
        );
        notification2.setId(notification2Id);

        when(notificationRepository.findByUserId(userId))
                .thenReturn(Arrays.asList(notification1, notification2));

        List<Notification> notifications = notificationService.findByUserId(userId);

        assertEquals(2, notifications.size());
        assertTrue(notifications.stream().allMatch(n -> n.getUserId().equals(userId)));

        verify(notificationRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Поиск уведомлений по подписке")
    void testFindBySubscriptionId() {
        Notification notification1 = new Notification(
                userId, subscription1Id, LocalDateTime.now(), NotificationType.UPCOMING_PAYMENT, "Message 1"
        );
        notification1.setId(notification1Id);

        when(notificationRepository.findBySubscriptionId(subscription1Id))
                .thenReturn(Arrays.asList(notification1));

        List<Notification> notifications = notificationService.findBySubscriptionId(subscription1Id);

        assertEquals(1, notifications.size());
        assertEquals(subscription1Id, notifications.get(0).getSubscriptionId());

        verify(notificationRepository, times(1)).findBySubscriptionId(subscription1Id);
    }

    @Test
    @DisplayName("Поиск уведомлений по статусу отправки")
    void testFindByIsSent() {
        Notification sentNotification = new Notification(
                userId, subscription1Id, LocalDateTime.now(), NotificationType.UPCOMING_PAYMENT, "Sent"
        );
        sentNotification.setId(notification1Id);
        sentNotification.setIsSent(true);

        when(notificationRepository.findByIsSent(true))
                .thenReturn(Arrays.asList(sentNotification));

        List<Notification> sentNotifications = notificationService.findByIsSent(true);

        assertEquals(1, sentNotifications.size());
        assertTrue(sentNotifications.get(0).getIsSent());

        verify(notificationRepository, times(1)).findByIsSent(true);
    }

    @Test
    @DisplayName("Получение ожидающих уведомлений")
    void testGetPendingNotifications() {
        Notification pendingNotification = new Notification(
                userId, subscription1Id, LocalDateTime.now().minusHours(1), NotificationType.UPCOMING_PAYMENT, "Pending"
        );
        pendingNotification.setId(notification1Id);
        pendingNotification.setIsSent(false);

        when(notificationRepository.findByNotificationDateBeforeAndIsSent(any(LocalDateTime.class), eq(false)))
                .thenReturn(Arrays.asList(pendingNotification));

        List<Notification> pending = notificationService.getPendingNotifications();

        assertEquals(1, pending.size());
        assertFalse(pending.get(0).getIsSent());

        verify(notificationRepository, times(1))
                .findByNotificationDateBeforeAndIsSent(any(LocalDateTime.class), eq(false));
    }

    @Test
    @DisplayName("Получение неотправленных уведомлений")
    void testGetUnsent() {
        Notification unsentNotification = new Notification(
                userId, subscription1Id, LocalDateTime.now(), NotificationType.UPCOMING_PAYMENT, "Unsent"
        );
        unsentNotification.setId(notification1Id);
        unsentNotification.setIsSent(false);

        when(notificationRepository.findByIsSent(false))
                .thenReturn(Arrays.asList(unsentNotification));

        List<Notification> unsent = notificationService.getUnsent();

        assertEquals(1, unsent.size());
        assertFalse(unsent.get(0).getIsSent());

        verify(notificationRepository, times(1)).findByIsSent(false);
    }

    @Test
    @DisplayName("Отметка уведомления как отправленного")
    void testMarkAsSent() {
        Notification notification = new Notification(
                userId, subscription1Id, LocalDateTime.now(), NotificationType.UPCOMING_PAYMENT, "Test"
        );
        notification.setId(testNotificationId);
        notification.setIsSent(false);

        when(notificationRepository.findById(testNotificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.markAsSent(testNotificationId);

        verify(notificationRepository, times(1)).findById(testNotificationId);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Обновление уведомления")
    void testUpdateNotification_Success() {
        Notification updatedNotification = new Notification(
                userId, subscription1Id, LocalDateTime.now(), NotificationType.UPCOMING_PAYMENT, "Updated message"
        );
        updatedNotification.setId(testNotificationId);

        when(notificationRepository.save(any(Notification.class))).thenReturn(updatedNotification);

        Notification updated = notificationService.update(updatedNotification);

        assertEquals("Updated message", updated.getMessage());
        assertEquals(testNotificationId, updated.getId());

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Удаление уведомления")
    void testDeleteNotification_Success() {
        doNothing().when(notificationRepository).deleteById(testNotificationId);

        notificationService.delete(testNotificationId);

        verify(notificationRepository, times(1)).deleteById(testNotificationId);
    }

    @Test
    @DisplayName("Получение общего количества уведомлений")
    void testGetTotalCount() {
        when(notificationRepository.count()).thenReturn(5L);

        long count = notificationService.getTotalCount();

        assertEquals(5L, count);

        verify(notificationRepository, times(1)).count();
    }
}
