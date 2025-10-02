package com.subscriptionmonitor.repository;

import com.subscriptionmonitor.model.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository для работы с подписками.
 *
 * @author Галанин А.Н.
 * @version 2.0 (ЛР2)
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Найти все подписки пользователя.
     */
    List<Subscription> findByUserId(Long userId);

    /**
     * Найти подписки пользователя по статусу активности.
     */
    List<Subscription> findByUserIdAndIsActive(Long userId, Boolean isActive);

    /**
     * Найти активные подписки пользователя (альтернативная версия).
     */
    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.isActive = true")
    List<Subscription> findActiveSubscriptionsByUserId(@Param("userId") Long userId);

    /**
     * Найти подписки по категории.
     */
    List<Subscription> findByCategoryId(Long categoryId);
}
