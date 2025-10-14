package com.subscriptionmonitor.repository;

import com.subscriptionmonitor.model.entity.Notification;
import com.subscriptionmonitor.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserId(UUID userId);

    List<Notification> findBySubscriptionId(UUID subscriptionId);

    List<Notification> findByIsSent(Boolean isSent);

    List<Notification> findByNotificationDateBeforeAndIsSent(LocalDateTime date, Boolean isSent);

    List<Notification> findByUserIdAndType(UUID userId, NotificationType type);
}
