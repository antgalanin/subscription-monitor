package com.subscriptionmonitor.repository;

import com.subscriptionmonitor.model.entity.Category;
import com.subscriptionmonitor.model.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByType(CategoryType type);

    List<Category> findByCreatedByUserId(UUID userId);

    List<Category> findByTypeAndCreatedByUserId(CategoryType type, UUID userId);

    Optional<Category> findByNameAndType(String name, CategoryType type);
}
