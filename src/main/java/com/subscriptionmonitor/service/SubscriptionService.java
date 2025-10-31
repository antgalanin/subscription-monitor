package com.subscriptionmonitor.service;

import com.subscriptionmonitor.exception.notfound.SubscriptionNotFoundException;
import com.subscriptionmonitor.exception.notfound.UserNotFoundException;
import com.subscriptionmonitor.exception.validation.NotificationValidationException;
import com.subscriptionmonitor.exception.validation.SubscriptionValidationException;
import com.subscriptionmonitor.model.entity.Notification;
import com.subscriptionmonitor.model.entity.Subscription;
import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.NotificationType;
import com.subscriptionmonitor.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    public Subscription create(Subscription subscription) throws SubscriptionValidationException {
        log.debug("Creating subscription: {}", subscription.getName());
        validateSubscription(subscription);
        Subscription saved = subscriptionRepository.save(subscription);

        try {
            createPaymentSuccessfulNotification(saved);
            createNotificationForSubscription(saved);
        } catch (Exception e) {
            log.error("Failed to create notifications for subscription {}: {}", saved.getId(), e.getMessage());
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public Subscription findById(UUID id) throws SubscriptionNotFoundException {
        log.debug("Finding subscription by id: {}", id);
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Subscription> findAll() {
        log.debug("Finding all subscriptions");
        return subscriptionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Subscription> findByUserId(UUID userId) {
        log.debug("Finding subscriptions by user id: {}", userId);
        return subscriptionRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Subscription> findByUserIdAndIsActive(UUID userId, Boolean isActive) {
        log.debug("Finding subscriptions by user {} and active status {}", userId, isActive);
        return subscriptionRepository.findByUserIdAndIsActive(userId, isActive);
    }

    @Transactional(readOnly = true)
    public List<Subscription> findActiveSubscriptionsByUserId(UUID userId) {
        log.debug("Finding active subscriptions by user: {}", userId);
        return subscriptionRepository.findActiveSubscriptionsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Subscription> findByCategoryId(UUID categoryId) {
        log.debug("Finding subscriptions by category: {}", categoryId);
        return subscriptionRepository.findByCategoryId(categoryId);
    }

    public Subscription update(Subscription subscription) throws SubscriptionNotFoundException, SubscriptionValidationException {
        log.debug("Updating subscription: {}", subscription.getId());
        if (subscription.getId() == null) {
            throw new SubscriptionValidationException("Subscription ID cannot be null for update operation");
        }
        if (!subscriptionRepository.existsById(subscription.getId())) {
            throw new SubscriptionNotFoundException(subscription.getId());
        }

        Subscription oldSubscription = findById(subscription.getId());
        String oldName = oldSubscription.getName();
        LocalDate oldNextBilling = oldSubscription.getPayment() != null ? oldSubscription.getPayment().getNextBillingDate() : null;
        Integer oldBillingPeriod = oldSubscription.getPayment() != null ? oldSubscription.getPayment().getBillingPeriodDays() : null;
        BigDecimal oldCost = oldSubscription.getPayment() != null ? oldSubscription.getPayment().getCost() : null;
        String oldCurrency = oldSubscription.getPayment() != null ? oldSubscription.getPayment().getCurrency().toString() : null;
        Boolean oldIsActive = oldSubscription.getIsActive();

        validateSubscription(subscription);
        Subscription updated = subscriptionRepository.save(subscription);

        String newName = updated.getName();
        LocalDate newNextBilling = updated.getPayment() != null ? updated.getPayment().getNextBillingDate() : null;
        Integer newBillingPeriod = updated.getPayment() != null ? updated.getPayment().getBillingPeriodDays() : null;
        BigDecimal newCost = updated.getPayment() != null ? updated.getPayment().getCost() : null;
        String newCurrency = updated.getPayment() != null ? updated.getPayment().getCurrency().toString() : null;
        Boolean newIsActive = updated.getIsActive();

        boolean nameChanged = !oldName.equals(newName);
        boolean billingDateChanged = newNextBilling != null && !newNextBilling.equals(oldNextBilling);
        boolean costChanged = newCost != null && !newCost.equals(oldCost);
        boolean currencyChanged = newCurrency != null && !newCurrency.equals(oldCurrency);
        boolean becameInactive = oldIsActive && !newIsActive;
        boolean becameActive = !oldIsActive && newIsActive;

        log.debug("Changes - name: {}, billing: {}, cost: {}, currency: {}, inactive: {}, active: {}",
                  nameChanged, billingDateChanged, costChanged, currencyChanged, becameInactive, becameActive);

        if (becameInactive) {
            log.info("Subscription became inactive, deleting unsent UPCOMING_PAYMENT notifications");
            deleteUnsentUpcomingNotifications(updated.getId());
        } else if (becameActive) {
            log.info("Subscription became active, creating UPCOMING_PAYMENT notification");
            try {
                createUpcomingPaymentIfMissing(updated);
            } catch (Exception e) {
                log.error("Failed to create UPCOMING_PAYMENT for subscription {}: {}", updated.getId(), e.getMessage());
            }
        } else if (billingDateChanged || nameChanged || costChanged || currencyChanged) {
            log.info("Subscription details changed, updating notifications");
            try {
                updateNotificationsForSubscription(updated, oldNextBilling, newNextBilling, oldBillingPeriod);
            } catch (Exception e) {
                log.error("Failed to update notifications for subscription {}: {}", updated.getId(), e.getMessage());
            }
        }

        return updated;
    }

    public Subscription updateWithPayment(UUID id, String name, UUID categoryId, Boolean isActive,
                                          BigDecimal cost, com.subscriptionmonitor.model.enums.Currency currency,
                                          Integer billingPeriodDays, LocalDate nextBillingDate,
                                          LocalDate oldNextBillingDate) throws SubscriptionNotFoundException, SubscriptionValidationException {
        log.debug("Updating subscription with payment: {}", id);

        Subscription subscription = findById(id);
        String oldName = subscription.getName();
        BigDecimal oldCost = subscription.getPayment().getCost();
        String oldCurrency = subscription.getPayment().getCurrency().toString();
        Boolean oldIsActive = subscription.getIsActive();

        subscription.setName(name);
        subscription.setCategoryId(categoryId);
        subscription.setIsActive(isActive);
        subscription.getPayment().setCost(cost);
        subscription.getPayment().setCurrency(currency);
        subscription.getPayment().setBillingPeriodDays(billingPeriodDays);
        subscription.getPayment().setNextBillingDate(nextBillingDate);

        validateSubscription(subscription);
        Subscription updated = subscriptionRepository.save(subscription);

        boolean nameChanged = !oldName.equals(name);
        boolean billingDateChanged = nextBillingDate != null && !nextBillingDate.equals(oldNextBillingDate);
        boolean costChanged = cost != null && !cost.equals(oldCost);
        boolean currencyChanged = currency != null && !currency.toString().equals(oldCurrency);
        boolean becameInactive = oldIsActive && !isActive;
        boolean becameActive = !oldIsActive && isActive;

        log.debug("Changes - name: {}, billing: {}, cost: {}, currency: {}, inactive: {}, active: {}",
                  nameChanged, billingDateChanged, costChanged, currencyChanged, becameInactive, becameActive);

        if (becameInactive) {
            log.info("Subscription became inactive, deleting unsent UPCOMING_PAYMENT notifications");
            deleteUnsentUpcomingNotifications(updated.getId());
        } else if (becameActive) {
            log.info("Subscription became active, creating UPCOMING_PAYMENT notification");
            try {
                createUpcomingPaymentIfMissing(updated);
            } catch (Exception e) {
                log.error("Failed to create UPCOMING_PAYMENT for subscription {}: {}", updated.getId(), e.getMessage());
            }
        } else if (billingDateChanged || nameChanged || costChanged || currencyChanged) {
            log.info("Subscription details changed, updating notifications");
            try {
                updateNotificationsForSubscription(updated, oldNextBillingDate, nextBillingDate, billingPeriodDays);
            } catch (Exception e) {
                log.error("Failed to update notifications for subscription {}: {}", updated.getId(), e.getMessage());
            }
        }

        return updated;
    }

    public Subscription deactivate(UUID id) throws SubscriptionNotFoundException {
        log.debug("Deactivating subscription: {}", id);
        Subscription subscription = findById(id);
        subscription.setIsActive(false);
        deleteUnsentUpcomingNotifications(id);
        return subscriptionRepository.save(subscription);
    }

    public void delete(UUID id) throws SubscriptionNotFoundException {
        log.debug("Deleting subscription: {}", id);
        if (!subscriptionRepository.existsById(id)) {
            throw new SubscriptionNotFoundException(id);
        }
        deleteAllNotifications(id);
        subscriptionRepository.deleteById(id);
    }

    public void deleteAll() {
        log.debug("Deleting all subscriptions");
        subscriptionRepository.deleteAll();
    }

    private void deleteUnsentUpcomingNotifications(UUID subscriptionId) {
        try {
            int deleted = notificationService.deleteUnsentBySubscriptionIdAndType(subscriptionId, NotificationType.UPCOMING_PAYMENT);
            log.debug("Deleted {} unsent UPCOMING_PAYMENT notifications for subscription {}", deleted, subscriptionId);
        } catch (Exception e) {
            log.error("Failed to delete unsent UPCOMING_PAYMENT notifications for subscription {}: {}", subscriptionId, e.getMessage());
        }
    }

    private void updateNotificationsForSubscription(Subscription subscription, LocalDate oldNextBilling, LocalDate newNextBilling, Integer oldBillingPeriod) throws UserNotFoundException, NotificationValidationException, com.subscriptionmonitor.exception.notfound.NotificationNotFoundException {
        if (!subscription.getIsActive()) {
            log.debug("Subscription {} is not active, skipping notification update", subscription.getId());
            return;
        }

        List<Notification> existingNotifications = notificationService.findBySubscriptionId(subscription.getId());
        User user = userService.findById(subscription.getUserId());
        int notificationDays = user.getNotificationDays();

        Notification paymentSuccessful = null;
        Notification upcomingPayment = null;

        for (Notification n : existingNotifications) {
            if (n.getType() == NotificationType.PAYMENT_SUCCESSFUL) {
                paymentSuccessful = n;
            } else if (n.getType() == NotificationType.UPCOMING_PAYMENT) {
                upcomingPayment = n;
            }
        }

        boolean billingDateChanged = newNextBilling != null && !newNextBilling.equals(oldNextBilling);

        if (paymentSuccessful != null) {
            if (billingDateChanged) {
                LocalDate newLastPaymentDate = newNextBilling.minusDays(subscription.getPayment().getBillingPeriodDays() != null ? subscription.getPayment().getBillingPeriodDays() : 30);
                LocalDateTime newPaymentDateTime = newLastPaymentDate.atStartOfDay();
                paymentSuccessful.setNotificationDate(newPaymentDateTime);

                if (newPaymentDateTime.isAfter(LocalDateTime.now())) {
                    paymentSuccessful.setIsSent(false);
                    log.debug("PAYMENT_SUCCESSFUL notification date moved to future, marking as unsent");
                }
            }

            String newMessage = String.format("Платеж выполнен успешно: %s - %.2f %s. Следующее списание: %s",
                    subscription.getName(),
                    subscription.getPayment().getCost(),
                    subscription.getPayment().getCurrency(),
                    subscription.getPayment().getNextBillingDate());
            paymentSuccessful.setMessage(newMessage);
            notificationService.update(paymentSuccessful);
            log.info("Updated PAYMENT_SUCCESSFUL notification for subscription {}", subscription.getId());
        } else if (billingDateChanged && oldNextBilling != null && newNextBilling.isAfter(oldNextBilling)) {
            createPaymentSuccessfulNotification(subscription);
        }

        if (upcomingPayment != null) {
            if (billingDateChanged) {
                LocalDate newNotificationDate = newNextBilling.minusDays(notificationDays);
                LocalDateTime newNotificationDateTime = newNotificationDate.atStartOfDay();
                upcomingPayment.setNotificationDate(newNotificationDateTime);

                if (newNotificationDateTime.isAfter(LocalDateTime.now())) {
                    upcomingPayment.setIsSent(false);
                    log.debug("UPCOMING_PAYMENT notification date in future, marking as unsent");
                } else {
                    upcomingPayment.setIsSent(true);
                    log.debug("UPCOMING_PAYMENT notification date in past, marking as sent");
                }
            }

            String newMessage = String.format("Предстоящий платеж: %s - %.2f %s (через %d дн.)",
                    subscription.getName(),
                    subscription.getPayment().getCost(),
                    subscription.getPayment().getCurrency(),
                    notificationDays);
            upcomingPayment.setMessage(newMessage);
            notificationService.update(upcomingPayment);
            log.info("Updated UPCOMING_PAYMENT notification for subscription {}", subscription.getId());
        } else if (upcomingPayment == null) {
            createNotificationForSubscription(subscription);
        }
    }

    private void createUpcomingPaymentIfMissing(Subscription subscription) throws UserNotFoundException, NotificationValidationException {
        if (!subscription.getIsActive()) {
            log.debug("Subscription {} is not active, skipping notification creation", subscription.getId());
            return;
        }

        List<Notification> existingNotifications = notificationService.findBySubscriptionId(subscription.getId());

        boolean hasPaymentSuccessful = existingNotifications.stream()
                .anyMatch(n -> n.getType() == NotificationType.PAYMENT_SUCCESSFUL);
        boolean hasUpcomingPayment = existingNotifications.stream()
                .anyMatch(n -> n.getType() == NotificationType.UPCOMING_PAYMENT);

        if (!hasPaymentSuccessful) {
            log.info("Creating missing PAYMENT_SUCCESSFUL notification for subscription {}", subscription.getId());
            createPaymentSuccessfulNotification(subscription);
        }

        if (!hasUpcomingPayment) {
            log.info("Creating missing UPCOMING_PAYMENT notification for subscription {}", subscription.getId());
            createNotificationForSubscription(subscription);
        }

        if (hasPaymentSuccessful && hasUpcomingPayment) {
            log.debug("All notifications already exist for subscription {}", subscription.getId());
        }
    }

    private void deleteAllNotifications(UUID subscriptionId) {
        try {
            int deleted = notificationService.deleteAllBySubscriptionId(subscriptionId);
            log.info("Deleted all {} notifications for subscription {}", deleted, subscriptionId);
        } catch (Exception e) {
            log.error("Failed to delete all notifications for subscription {}: {}", subscriptionId, e.getMessage());
        }
    }

    private void validateSubscription(Subscription subscription) throws SubscriptionValidationException {
        if (subscription == null) {
            throw new SubscriptionValidationException("Subscription cannot be null");
        }
        if (subscription.getName() == null || subscription.getName().trim().isEmpty()) {
            throw new SubscriptionValidationException("Subscription name cannot be empty");
        }
        if (subscription.getName().length() > 200) {
            throw new SubscriptionValidationException("Subscription name cannot exceed 200 characters");
        }
        if (subscription.getUserId() == null) {
            throw new SubscriptionValidationException("User ID cannot be null");
        }
        if (subscription.getCategoryId() == null) {
            throw new SubscriptionValidationException("Category ID cannot be null");
        }
        if (subscription.getPayment() == null) {
            throw new SubscriptionValidationException("Payment information cannot be null");
        }
    }

    private void createNotificationForSubscription(Subscription subscription) throws UserNotFoundException, NotificationValidationException {
        if (!subscription.getIsActive()) {
            log.debug("Subscription {} is not active, skipping notification creation", subscription.getId());
            return;
        }

        if (subscription.getPayment() == null || subscription.getPayment().getNextBillingDate() == null) {
            log.warn("Subscription {} has no payment or next billing date, skipping notification", subscription.getId());
            return;
        }

        User user = userService.findById(subscription.getUserId());
        LocalDate nextBillingDate = subscription.getPayment().getNextBillingDate();
        int notificationDays = user.getNotificationDays();

        LocalDate notificationDate = nextBillingDate.minusDays(notificationDays);
        LocalDateTime notificationDateTime = notificationDate.atStartOfDay();

        String message = String.format("Предстоящий платеж: %s - %.2f %s (через %d дн.)",
                subscription.getName(),
                subscription.getPayment().getCost(),
                subscription.getPayment().getCurrency(),
                notificationDays);

        Notification notification = new Notification(
                subscription.getUserId(),
                subscription.getId(),
                notificationDateTime,
                NotificationType.UPCOMING_PAYMENT,
                message
        );

        notificationService.create(notification);
        log.info("Created UPCOMING_PAYMENT notification for subscription {} (notification date: {})", subscription.getId(), notificationDateTime);
    }

    private void createPaymentSuccessfulNotification(Subscription subscription) throws UserNotFoundException, NotificationValidationException {
        if (!subscription.getIsActive()) {
            log.debug("Subscription {} is not active, skipping payment successful notification", subscription.getId());
            return;
        }

        if (subscription.getPayment() == null) {
            log.warn("Subscription {} has no payment info, skipping payment successful notification", subscription.getId());
            return;
        }

        LocalDate nextBillingDate = subscription.getPayment().getNextBillingDate();
        Integer billingPeriodDays = subscription.getPayment().getBillingPeriodDays();

        LocalDate lastPaymentDate = nextBillingDate.minusDays(billingPeriodDays != null ? billingPeriodDays : 30);
        LocalDateTime paymentDateTime = lastPaymentDate.atStartOfDay();

        String message = String.format("Платеж выполнен успешно: %s - %.2f %s. Следующее списание: %s",
                subscription.getName(),
                subscription.getPayment().getCost(),
                subscription.getPayment().getCurrency(),
                subscription.getPayment().getNextBillingDate());

        Notification notification = new Notification(
                subscription.getUserId(),
                subscription.getId(),
                paymentDateTime,
                NotificationType.PAYMENT_SUCCESSFUL,
                message
        );

        notificationService.create(notification);
        log.info("Created PAYMENT_SUCCESSFUL notification for subscription {} (payment date: {})", subscription.getId(), paymentDateTime);
    }
}
