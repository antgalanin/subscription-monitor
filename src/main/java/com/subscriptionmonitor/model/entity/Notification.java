package com.subscriptionmonitor.model.entity;

import com.subscriptionmonitor.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Notification extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Column(name = "notification_date", nullable = false)
    private LocalDateTime notificationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "is_sent", nullable = false)
    private Boolean isSent = false;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    public Notification(Long userId, Long subscriptionId, LocalDateTime notificationDate,
                       NotificationType type, String message) {
        super();
        this.userId = userId;
        this.subscriptionId = subscriptionId;
        this.notificationDate = notificationDate;
        this.type = type;
        this.isSent = false;
        this.message = message;
    }
}
