package com.subscriptionmonitor.service;

import com.subscriptionmonitor.model.entity.Category;
import com.subscriptionmonitor.storage.DataStorage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CategoryService implements CrudService<Category, Long> {
    private final DataStorage storage;

    public CategoryService() {
        this.storage = DataStorage.getInstance();
    }

    @Override
    public Category create(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }

        Long id = storage.generateCategoryId();
        category.setId(id);
        storage.getCategories().put(id, category);
        return category;
    }

    @Override
    public Optional<Category> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.getCategories().get(id));
    }

    @Override
    public List<Category> findAll() {
        return storage.getCategories().values().stream().collect(Collectors.toList());
    }

    @Override
    public Category update(Category category) {
        if (category == null || category.getId() == null) {
            throw new IllegalArgumentException("Category and category ID cannot be null");
        }

        if (!storage.getCategories().containsKey(category.getId())) {
            throw new IllegalArgumentException("Category not found with ID: " + category.getId());
        }

        storage.getCategories().put(category.getId(), category);
        return category;
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        return storage.getCategories().remove(id) != null;
    }

    public List<Category> getDefaultCategories() {
        return storage.getCategories().values().stream()
                .filter(category -> Boolean.TRUE.equals(category.getIsDefault()))
                .collect(Collectors.toList());
    }

    public List<Category> getCategoriesByUser(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        return storage.getCategories().values().stream()
                .filter(category -> userId.equals(category.getCreatedByUserId()))
                .collect(Collectors.toList());
    }

    public List<Category> getAvailableCategoriesForUser(Long userId) {
        if (userId == null) {
            return getDefaultCategories();
        }

        return storage.getCategories().values().stream()
                .filter(category -> Boolean.TRUE.equals(category.getIsDefault())
                                 || userId.equals(category.getCreatedByUserId()))
                .collect(Collectors.toList());
    }

    public Optional<Category> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }

        return storage.getCategories().values().stream()
                .filter(category -> name.equals(category.getName()))
                .findFirst();
    }

    public int getTotalCount() {
        return storage.getTotalCategoriesCount();
    }
}