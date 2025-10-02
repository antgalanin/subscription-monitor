package com.subscriptionmonitor.repository;

import com.subscriptionmonitor.model.entity.Notification;
import com.subscriptionmonitor.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository для работы с уведомлениями.
 *
 * @author Галанин А.Н.
 * @version 2.0 (ЛР2 - новая модель)
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Найти все уведомления пользователя.
     */
    List<Notification> findByUserId(Long userId);

    /**
     * Найти уведомления по подписке.
     */
    List<Notification> findBySubscriptionId(Long subscriptionId);

    /**
     * Найти уведомления по статусу отправки.
     */
    List<Notification> findByIsSent(Boolean isSent);

    /**
     * Найти неотправленные уведомления с датой до указанной.
     * Используется для получения уведомлений, которые пора отправить.
     */
    List<Notification> findByNotificationDateBeforeAndIsSent(LocalDateTime date, Boolean isSent);

    /**
     * Найти уведомления пользователя по типу.
     */
    List<Notification> findByUserIdAndType(Long userId, NotificationType type);
}
