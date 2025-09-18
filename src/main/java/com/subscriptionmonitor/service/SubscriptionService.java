package com.subscriptionmonitor.service;

import com.subscriptionmonitor.model.entity.Subscription;
import com.subscriptionmonitor.model.enums.Currency;
import com.subscriptionmonitor.storage.DataStorage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SubscriptionService implements CrudService<Subscription, Long> {
    private final DataStorage storage;

    public SubscriptionService() {
        this.storage = DataStorage.getInstance();
    }

    @Override
    public Subscription create(Subscription subscription) {
        if (subscription == null) {
            throw new IllegalArgumentException("Subscription cannot be null");
        }

        validateSubscription(subscription);

        Long id = storage.generateSubscriptionId();
        subscription.setId(id);
        storage.getSubscriptions().put(id, subscription);
        return subscription;
    }

    @Override
    public Optional<Subscription> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.getSubscriptions().get(id));
    }

    @Override
    public List<Subscription> findAll() {
        return storage.getSubscriptions().values().stream().collect(Collectors.toList());
    }

    @Override
    public Subscription update(Subscription subscription) {
        if (subscription == null || subscription.getId() == null) {
            throw new IllegalArgumentException("Subscription and subscription ID cannot be null");
        }

        if (!storage.getSubscriptions().containsKey(subscription.getId())) {
            throw new IllegalArgumentException("Subscription not found with ID: " + subscription.getId());
        }

        validateSubscription(subscription);
        storage.getSubscriptions().put(subscription.getId(), subscription);
        return subscription;
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        return storage.getSubscriptions().remove(id) != null;
    }

    public List<Subscription> getSubscriptionsByUser(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        return storage.getSubscriptions().values().stream()
                .filter(subscription -> userId.equals(subscription.getUserId()))
                .collect(Collectors.toList());
    }

    public List<Subscription> getActiveSubscriptions(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        return storage.getSubscriptions().values().stream()
                .filter(subscription -> userId.equals(subscription.getUserId())
                                     && Boolean.TRUE.equals(subscription.getIsActive()))
                .collect(Collectors.toList());
    }

    public List<Subscription> getSubscriptionsByCurrency(Currency currency) {
        if (currency == null) {
            return Collections.emptyList();
        }

        return storage.getSubscriptions().values().stream()
                .filter(subscription -> currency.equals(subscription.getCurrency()))
                .collect(Collectors.toList());
    }

    public List<Subscription> getSubscriptionsByUserAndCurrency(Long userId, Currency currency) {
        if (userId == null || currency == null) {
            return Collections.emptyList();
        }

        return storage.getSubscriptions().values().stream()
                .filter(subscription -> userId.equals(subscription.getUserId())
                                     && currency.equals(subscription.getCurrency()))
                .collect(Collectors.toList());
    }

    public List<Subscription> getUpcomingBillings(Long userId, int days) {
        if (userId == null) {
            return Collections.emptyList();
        }

        LocalDate targetDate = LocalDate.now().plusDays(days);

        return storage.getSubscriptions().values().stream()
                .filter(subscription -> userId.equals(subscription.getUserId())
                                     && Boolean.TRUE.equals(subscription.getIsActive())
                                     && subscription.getNextBillingDate() != null
                                     && !subscription.getNextBillingDate().isAfter(targetDate))
                .collect(Collectors.toList());
    }

    public boolean deactivateSubscription(Long id) {
        Optional<Subscription> subscription = findById(id);
        if (subscription.isPresent()) {
            subscription.get().setIsActive(false);
            return true;
        }
        return false;
    }

    public boolean activateSubscription(Long id) {
        Optional<Subscription> subscription = findById(id);
        if (subscription.isPresent()) {
            subscription.get().setIsActive(true);
            return true;
        }
        return false;
    }

    public BigDecimal calculateTotalMonthlyCost(Long userId, Currency currency) {
        if (userId == null || currency == null) {
            return BigDecimal.ZERO;
        }

        return getActiveSubscriptions(userId).stream()
                .filter(subscription -> currency.equals(subscription.getCurrency()))
                .map(subscription -> {
                    BigDecimal monthlyCost = subscription.getCost();
                    if (subscription.getBillingPeriodDays() != 30) {
                        monthlyCost = monthlyCost.multiply(BigDecimal.valueOf(30))
                                               .divide(BigDecimal.valueOf(subscription.getBillingPeriodDays()), 2, BigDecimal.ROUND_HALF_UP);
                    }
                    return monthlyCost;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalCount() {
        return storage.getTotalSubscriptionsCount();
    }

    private void validateSubscription(Subscription subscription) {
        if (subscription.getName() == null || subscription.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Subscription name cannot be null or empty");
        }

        if (subscription.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (subscription.getCost() == null || subscription.getCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost cannot be null or negative");
        }

        if (subscription.getBillingPeriodDays() == null || subscription.getBillingPeriodDays() <= 0) {
            throw new IllegalArgumentException("Billing period must be positive");
        }
    }
}