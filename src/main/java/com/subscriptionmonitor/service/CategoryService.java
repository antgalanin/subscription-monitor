package com.subscriptionmonitor.service;

import com.subscriptionmonitor.exception.notfound.CategoryNotFoundException;
import com.subscriptionmonitor.exception.validation.CategoryValidationException;
import com.subscriptionmonitor.exception.special.LegacyCategoryException;
import com.subscriptionmonitor.model.entity.Category;
import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.CategoryType;
import com.subscriptionmonitor.model.enums.UserRole;
import com.subscriptionmonitor.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
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

    public Category create(Category category, User currentUser) throws CategoryValidationException, LegacyCategoryException {
        log.debug("Creating category: {}", category.getName());
        validateCategory(category);

        if (category.getType() == CategoryType.LEGACY) {
            throw new LegacyCategoryException(category.getName());
        }

        if (currentUser != null) {
            if (currentUser.getRole() == UserRole.ADMIN) {
                if (category.getType() == CategoryType.SYSTEM) {
                    category.setCreatedByUserId(null);
                } else if (category.getType() == CategoryType.CUSTOM) {
                    category.setCreatedByUserId(currentUser.getId());
                }
            } else if (currentUser.getRole() == UserRole.USER) {
                if (category.getType() != CategoryType.CUSTOM) {
                    throw new AccessDeniedException("USER can only create CUSTOM categories");
                }
                category.setCreatedByUserId(currentUser.getId());
            }
        }

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
        if (category.getType() == CategoryType.LEGACY) {
            throw new LegacyCategoryException(category.getId());
        }
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

    public Category update(Category category, User currentUser) throws CategoryNotFoundException, CategoryValidationException, LegacyCategoryException {
        log.debug("Updating category: {}", category.getId());

        if (category.getId() == null) {
            throw new CategoryValidationException("Category ID cannot be null for update operation");
        }

        Category existing = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new CategoryNotFoundException(category.getId()));

        if (currentUser.getRole() == UserRole.USER) {
            if (existing.getType() == CategoryType.SYSTEM) {
                throw new AccessDeniedException("Users cannot modify SYSTEM categories");
            }
            if (existing.getType() == CategoryType.LEGACY) {
                throw new LegacyCategoryException(existing.getId());
            }
            if (existing.getCreatedByUserId() == null || !existing.getCreatedByUserId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Users can only modify their own categories");
            }
            if (category.getType() == CategoryType.SYSTEM) {
                throw new AccessDeniedException("Users cannot change category type to SYSTEM");
            }
        }

        validateCategory(category);

        if (category.getType() == CategoryType.SYSTEM) {
            category.setCreatedByUserId(null);
        } else if (category.getType() == CategoryType.CUSTOM && category.getCreatedByUserId() == null) {
            category.setCreatedByUserId(currentUser.getId());
        }

        return categoryRepository.save(category);
    }

    public void delete(UUID id, User currentUser) throws CategoryNotFoundException, LegacyCategoryException {
        log.debug("Deleting category: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (category.getType() == CategoryType.LEGACY) {
            throw new LegacyCategoryException(category.getId());
        }

        if (currentUser.getRole() == UserRole.USER) {
            if (category.getType() == CategoryType.SYSTEM) {
                throw new AccessDeniedException("Users cannot delete SYSTEM categories");
            }
            if (category.getCreatedByUserId() == null || !category.getCreatedByUserId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Users can only delete their own categories");
            }
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

}
