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

    List<Subscription> findByIsActiveTrue();

    @Query("SELECT s FROM Subscription s LEFT JOIN FETCH s.payment WHERE s.isActive = true")
    List<Subscription> findActiveWithPayment();

    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.isActive = true")
    List<Subscription> findActiveSubscriptionsByUserId(@Param("userId") UUID userId);

    List<Subscription> findByCategoryId(UUID categoryId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Subscription s WHERE s.payment.id = :paymentId AND s.userId = :userId")
    boolean existsByPaymentIdAndUserId(@Param("paymentId") UUID paymentId, @Param("userId") UUID userId);
}
