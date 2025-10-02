package com.subscriptionmonitor.repository;

import com.subscriptionmonitor.model.entity.Category;
import com.subscriptionmonitor.model.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository для работы с категориями.
 *
 * @author Галанин А.Н.
 * @version 2.0 (ЛР2)
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Найти все категории указанного типа.
     */
    List<Category> findByType(CategoryType type);

    /**
     * Найти категории, созданные конкретным пользователем.
     */
    List<Category> findByCreatedByUserId(Long userId);

    /**
     * Найти категории по типу и создателю.
     */
    List<Category> findByTypeAndCreatedByUserId(CategoryType type, Long userId);
}
