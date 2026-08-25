package com.subscriptionmonitor.scheduler;

import com.subscriptionmonitor.model.entity.Notification;
import com.subscriptionmonitor.model.entity.Payment;
import com.subscriptionmonitor.model.entity.Subscription;
import com.subscriptionmonitor.model.enums.NotificationType;
import com.subscriptionmonitor.repository.NotificationRepository;
import com.subscriptionmonitor.repository.PaymentRepository;
import com.subscriptionmonitor.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessor {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void processOverduePaymentsOnStartup() {
        log.info("Starting overdue payments processing on application startup...");
        processOverduePayments();
        log.info("Finished overdue payments processing on startup");
    }

    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void processOverduePaymentsDaily() {
        log.info("Starting scheduled overdue payments processing (daily at 00:01)...");
        processOverduePayments();
        log.info("Finished scheduled overdue payments processing");
    }

    public void processOverduePayments() {
        LocalDate today = LocalDate.now();

        List<Subscription> activeSubscriptions = subscriptionRepository.findByIsActiveTrue();

        int processedCount = 0;
        int notificationsCreated = 0;

        for (Subscription subscription : activeSubscriptions) {
            Payment payment = subscription.getPayment();

            if (payment == null || payment.getNextBillingDate() == null) {
                continue;
            }

            LocalDate nextBillingDate = payment.getNextBillingDate();

            if (nextBillingDate.isBefore(today)) {
                long daysPassed = java.time.temporal.ChronoUnit.DAYS.between(nextBillingDate, today);
                int billingPeriod = payment.getBillingPeriodDays() != null ? payment.getBillingPeriodDays() : 30;

                long periodsToAdvance = (daysPassed / billingPeriod) + 1;

                for (int i = 0; i < periodsToAdvance; i++) {
                    createPaymentSuccessfulNotification(subscription, payment);
                    notificationsCreated++;
                }

                LocalDate newNextBillingDate = nextBillingDate.plusDays(billingPeriod * periodsToAdvance);
                payment.setNextBillingDate(newNextBillingDate);
                paymentRepository.save(payment);

                processedCount++;
                log.debug("Updated payment {} for subscription {} - advanced {} periods to {}",
                        payment.getId(), subscription.getName(), periodsToAdvance, newNextBillingDate);
            }
        }

        if (processedCount > 0) {
            log.info("Processed {} overdue payments, created {} notifications", processedCount, notificationsCreated);
        }
    }

    private void createPaymentSuccessfulNotification(Subscription subscription, Payment payment) {
        try {
            LocalDate billingDate = payment.getNextBillingDate();
            java.time.LocalDateTime startOfDay = billingDate.atStartOfDay();
            java.time.LocalDateTime endOfDay = billingDate.atTime(23, 59, 59);

            Notification existingNotification = notificationRepository
                    .findTopBySubscriptionIdAndTypeAndNotificationDateBetweenOrderByCreatedAtDesc(
                            subscription.getId(),
                            NotificationType.PAYMENT_SUCCESSFUL,
                            startOfDay,
                            endOfDay
                    );

            if (existingNotification != null) {
                return;
            }

            Notification notification = new Notification();
            notification.setId(UUID.randomUUID());
            notification.setUserId(subscription.getUserId());
            notification.setSubscriptionId(subscription.getId());
            notification.setType(NotificationType.PAYMENT_SUCCESSFUL);
            notification.setMessage(String.format(
                    "Списание по подписке '%s' выполнено успешно. Сумма: %.2f %s",
                    subscription.getName(),
                    payment.getCost(),
                    payment.getCurrency()
            ));
            notification.setNotificationDate(startOfDay);
            notification.setIsSent(false);

            notificationRepository.save(notification);
            log.debug("Created PAYMENT_SUCCESSFUL notification for subscription {} on date {}",
                    subscription.getName(), billingDate);
        } catch (Exception e) {
            log.error("Error creating payment notification: {}", e.getMessage(), e);
        }
    }
}
