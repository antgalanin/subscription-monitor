package com.subscriptionmonitor.service;

import com.subscriptionmonitor.model.entity.Category;
import com.subscriptionmonitor.model.enums.CategoryType;
import com.subscriptionmonitor.repository.CategoryRepository;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category create(Category category) {
        log.debug("Creating category: {}", category.getName());
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Optional<Category> findById(UUID id) {
        log.debug("Finding category by id: {}", id);
        return categoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        log.debug("Finding all categories");
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Category> findByType(CategoryType type) {
        log.debug("Finding categories by type: {}", type);
        return categoryRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<Category> findByCreatedByUserId(UUID userId) {
        log.debug("Finding categories created by user: {}", userId);
        return categoryRepository.findByCreatedByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Category> findByTypeAndCreatedByUserId(CategoryType type, UUID userId) {
        log.debug("Finding categories by type {} and user {}", type, userId);
        return categoryRepository.findByTypeAndCreatedByUserId(type, userId);
    }

    public Category update(Category category) {
        log.debug("Updating category: {}", category.getId());
        return categoryRepository.save(category);
    }

    public void delete(UUID id) {
        log.debug("Deleting category: {}", id);
        categoryRepository.deleteById(id);
    }

    public void deleteAll() {
        log.debug("Deleting all categories");
        categoryRepository.deleteAll();
    }
}
