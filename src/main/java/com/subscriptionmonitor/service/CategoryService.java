package com.subscriptionmonitor.service;

import com.subscriptionmonitor.exception.CategoryNotFoundException;
import com.subscriptionmonitor.exception.CategoryValidationException;
import com.subscriptionmonitor.exception.LegacyCategoryException;
import com.subscriptionmonitor.model.entity.Category;
import com.subscriptionmonitor.model.enums.CategoryType;
import com.subscriptionmonitor.repository.CategoryRepository;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category create(Category category) throws CategoryValidationException, LegacyCategoryException {
        log.debug("Creating category: {}", category.getName());
        validateCategory(category);
        checkLegacyType(category);
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Category findById(UUID id) throws CategoryNotFoundException {
        log.debug("Finding category by id: {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Category findByIdAndValidate(UUID id) throws CategoryNotFoundException, LegacyCategoryException {
        Category category = findById(id);
        checkLegacyType(category);
        return category;
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

    public Category update(Category category) throws CategoryNotFoundException, CategoryValidationException, LegacyCategoryException {
        log.debug("Updating category: {}", category.getId());
        if (category.getId() == null) {
            throw new CategoryValidationException("Category ID cannot be null for update operation");
        }
        if (!categoryRepository.existsById(category.getId())) {
            throw new CategoryNotFoundException(category.getId());
        }
        validateCategory(category);
        checkLegacyType(category);
        return categoryRepository.save(category);
    }

    public void delete(UUID id) throws CategoryNotFoundException {
        log.debug("Deleting category: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException(id);
        }
        categoryRepository.deleteById(id);
    }

    public void deleteAll() {
        log.debug("Deleting all categories");
        categoryRepository.deleteAll();
    }

    private void validateCategory(Category category) throws CategoryValidationException {
        if (category == null) {
            throw new CategoryValidationException("Category cannot be null");
        }
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new CategoryValidationException("Category name cannot be empty");
        }
        if (category.getName().length() > 100) {
            throw new CategoryValidationException("Category name cannot exceed 100 characters");
        }
        if (category.getType() == null) {
            throw new CategoryValidationException("Category type cannot be null");
        }
    }

    private void checkLegacyType(Category category) throws LegacyCategoryException {
        if (category.getType() == CategoryType.LEGACY) {
            if (category.getId() != null) {
                throw new LegacyCategoryException(category.getId());
            } else {
                throw new LegacyCategoryException(category.getName());
            }
        }
    }
}
