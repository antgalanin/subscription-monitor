package com.subscriptionmonitor.scheduler;

import com.subscriptionmonitor.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    public void markPendingNotificationsAsSent() {
        try {
            int marked = notificationService.markPendingAsSent();
            if (marked > 0) {
                log.info("Scheduled task: Marked {} notifications as sent", marked);
            }
        } catch (Exception e) {
            log.error("Error marking pending notifications as sent: {}", e.getMessage(), e);
        }
    }
}
