package com.subscriptionmonitor.service;

import com.subscriptionmonitor.exception.notfound.NotificationNotFoundException;
import com.subscriptionmonitor.exception.validation.NotificationValidationException;
import com.subscriptionmonitor.model.entity.Notification;
import com.subscriptionmonitor.model.enums.NotificationType;
import com.subscriptionmonitor.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification create(Notification notification) throws NotificationValidationException {
        log.debug("Creating notification for user {} and subscription {}",
                notification.getUserId(), notification.getSubscriptionId());
        validateNotification(notification);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Notification findById(UUID id) throws NotificationNotFoundException {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
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

    public void markAsSent(UUID id) throws NotificationNotFoundException {
        log.debug("Marking notification {} as sent", id);
        Notification notification = findById(id);
        notification.setIsSent(true);
        notificationRepository.save(notification);
    }

    public Notification update(Notification notification) throws NotificationNotFoundException, NotificationValidationException {
        log.debug("Updating notification: {}", notification.getId());
        if (notification.getId() == null) {
            throw new NotificationValidationException("Notification ID cannot be null for update operation");
        }
        if (!notificationRepository.existsById(notification.getId())) {
            throw new NotificationNotFoundException(notification.getId());
        }
        validateNotification(notification);
        return notificationRepository.save(notification);
    }

    public void delete(UUID id) throws NotificationNotFoundException {
        log.debug("Deleting notification: {}", id);
        if (!notificationRepository.existsById(id)) {
            throw new NotificationNotFoundException(id);
        }
        notificationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long getTotalCount() {
        return notificationRepository.count();
    }

    private void validateNotification(Notification notification) throws NotificationValidationException {
        if (notification == null) {
            throw new NotificationValidationException("Notification cannot be null");
        }
        if (notification.getUserId() == null) {
            throw new NotificationValidationException("User ID cannot be null");
        }
        if (notification.getSubscriptionId() == null) {
            throw new NotificationValidationException("Subscription ID cannot be null");
        }
        if (notification.getNotificationDate() == null) {
            throw new NotificationValidationException("Notification date cannot be null");
        }
        if (notification.getType() == null) {
            throw new NotificationValidationException("Notification type cannot be null");
        }
        if (notification.getMessage() == null || notification.getMessage().trim().isEmpty()) {
            throw new NotificationValidationException("Notification message cannot be empty");
        }
    }
}
