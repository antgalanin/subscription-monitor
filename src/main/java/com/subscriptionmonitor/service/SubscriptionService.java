package com.subscriptionmonitor.service;

import com.subscriptionmonitor.model.entity.Subscription;
import com.subscriptionmonitor.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public Subscription create(Subscription subscription) {
        log.debug("Creating subscription: {}", subscription.getName());
        return subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    public Optional<Subscription> findById(UUID id) {
        log.debug("Finding subscription by id: {}", id);
        return subscriptionRepository.findById(id);
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

    public Subscription update(Subscription subscription) {
        log.debug("Updating subscription: {}", subscription.getId());
        return subscriptionRepository.save(subscription);
    }

    public Optional<Subscription> deactivate(UUID id) {
        log.debug("Deactivating subscription: {}", id);
        return subscriptionRepository.findById(id).map(subscription -> {
            subscription.setIsActive(false);
            return subscriptionRepository.save(subscription);
        });
    }

    public void delete(UUID id) {
        log.debug("Deleting subscription: {}", id);
        subscriptionRepository.deleteById(id);
    }

    public void deleteAll() {
        log.debug("Deleting all subscriptions");
        subscriptionRepository.deleteAll();
    }
}
