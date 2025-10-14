package com.subscriptionmonitor.service;

import com.subscriptionmonitor.exception.notfound.SubscriptionNotFoundException;
import com.subscriptionmonitor.exception.validation.SubscriptionValidationException;
import com.subscriptionmonitor.model.entity.Subscription;
import com.subscriptionmonitor.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public Subscription create(Subscription subscription) throws SubscriptionValidationException {
        log.debug("Creating subscription: {}", subscription.getName());
        validateSubscription(subscription);
        return subscriptionRepository.save(subscription);
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
        validateSubscription(subscription);
        return subscriptionRepository.save(subscription);
    }

    public Subscription deactivate(UUID id) throws SubscriptionNotFoundException {
        log.debug("Deactivating subscription: {}", id);
        Subscription subscription = findById(id);
        subscription.setIsActive(false);
        return subscriptionRepository.save(subscription);
    }

    public void delete(UUID id) throws SubscriptionNotFoundException {
        log.debug("Deleting subscription: {}", id);
        if (!subscriptionRepository.existsById(id)) {
            throw new SubscriptionNotFoundException(id);
        }
        subscriptionRepository.deleteById(id);
    }

    public void deleteAll() {
        log.debug("Deleting all subscriptions");
        subscriptionRepository.deleteAll();
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
}
