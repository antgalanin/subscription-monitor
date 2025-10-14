package com.subscriptionmonitor.service;

import com.subscriptionmonitor.model.entity.Notification;
import com.subscriptionmonitor.model.enums.NotificationType;
import com.subscriptionmonitor.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification create(Notification notification) {
        log.debug("Creating notification for user {} and subscription {}",
                notification.getUserId(), notification.getSubscriptionId());
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Optional<Notification> findById(UUID id) {
        return notificationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Notification> findByUserId(UUID userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> findBySubscriptionId(UUID subscriptionId) {
        return notificationRepository.findBySubscriptionId(subscriptionId);
    }

    @Transactional(readOnly = true)
    public List<Notification> findByIsSent(Boolean isSent) {
        return notificationRepository.findByIsSent(isSent);
    }

    @Transactional(readOnly = true)
    public List<Notification> findByUserIdAndType(UUID userId, NotificationType type) {
        return notificationRepository.findByUserIdAndType(userId, type);
    }

    @Transactional(readOnly = true)
    public List<Notification> getPendingNotifications() {
        return notificationRepository.findByNotificationDateBeforeAndIsSent(
                LocalDateTime.now(), false);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnsent() {
        return notificationRepository.findByIsSent(false);
    }

    public void markAsSent(UUID id) {
        log.debug("Marking notification {} as sent", id);
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setIsSent(true);
            notificationRepository.save(notification);
        });
    }

    public Notification update(Notification notification) {
        log.debug("Updating notification: {}", notification.getId());
        return notificationRepository.save(notification);
    }

    public void delete(UUID id) {
        log.debug("Deleting notification: {}", id);
        notificationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long getTotalCount() {
        return notificationRepository.count();
    }
}
