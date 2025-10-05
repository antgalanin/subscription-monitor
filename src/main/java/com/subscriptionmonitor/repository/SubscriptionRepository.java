package com.subscriptionmonitor.repository;

import com.subscriptionmonitor.model.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByUserId(UUID userId);

    List<Subscription> findByUserIdAndIsActive(UUID userId, Boolean isActive);

    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.isActive = true")
    List<Subscription> findActiveSubscriptionsByUserId(@Param("userId") UUID userId);

    List<Subscription> findByCategoryId(UUID categoryId);
}
