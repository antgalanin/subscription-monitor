package com.subscriptionmonitor.repository;

import com.subscriptionmonitor.model.entity.Notification;
import com.subscriptionmonitor.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    List<Notification> findByUserIdAndNotificationDateBefore(UUID userId, LocalDateTime date);

    Notification findTopBySubscriptionIdAndTypeAndNotificationDateBetweenOrderByCreatedAtDesc(
            UUID subscriptionId,
            NotificationType type,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.subscriptionId = :subscriptionId AND n.isSent = false AND n.type = :type")
    int deleteUnsentBySubscriptionIdAndType(@Param("subscriptionId") UUID subscriptionId,
                                            @Param("type") NotificationType type);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.subscriptionId = :subscriptionId")
    int deleteAllBySubscriptionId(@Param("subscriptionId") UUID subscriptionId);
}
