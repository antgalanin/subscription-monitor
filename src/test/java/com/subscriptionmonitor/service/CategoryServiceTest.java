package com.subscriptionmonitor.service;

import com.subscriptionmonitor.model.entity.Category;
import com.subscriptionmonitor.model.enums.CategoryType;
import com.subscriptionmonitor.storage.DataStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CategoryService Tests")
class CategoryServiceTest {

    private CategoryService categoryService;
    private DataStorage storage;

    @BeforeEach
    void setUp() {
        storage = DataStorage.getInstance();
        storage.clearAll();
        categoryService = new CategoryService();
    }

    @Test
    @DisplayName("Создание категории с корректными данными")
    void testCreateCategory_Success() {
        Category category = new Category("Развлечения", CategoryType.SYSTEM, 1L);

        Category created = categoryService.create(category);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertNotNull(created.getUuid());
        assertEquals("Развлечения", created.getName());
        assertEquals(CategoryType.SYSTEM, created.getType());
        assertEquals(1L, created.getCreatedByUserId());
        assertEquals(1, categoryService.getTotalCount());
    }

    @Test
    @DisplayName("Создание категории с null")
    void testCreateCategory_Null() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> categoryService.create(null));

        assertEquals("Category cannot be null", exception.getMessage());
        assertEquals(0, categoryService.getTotalCount());
    }

    @Test
    @DisplayName("Создание категории с пустым именем")
    void testCreateCategory_EmptyName() {
        Category category = new Category("", CategoryType.SYSTEM, 1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> categoryService.create(category));

        assertTrue(exception.getMessage().contains("name cannot be null or empty"));
        assertEquals(0, categoryService.getTotalCount());
    }

    @Test
    @DisplayName("Создание категории с null именем")
    void testCreateCategory_NullName() {
        Category category = new Category(null, CategoryType.SYSTEM, 1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> categoryService.create(category));

        assertTrue(exception.getMessage().contains("name cannot be null or empty"));
        assertEquals(0, categoryService.getTotalCount());
    }

    @Test
    @DisplayName("Поиск категории по ID")
    void testFindById_Success() {
        Category category = new Category("Образование", CategoryType.CUSTOM, 2L);
        Category created = categoryService.create(category);

        Optional<Category> found = categoryService.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("Образование", found.get().getName());
    }

    @Test
    @DisplayName("Поиск категории по несуществующему ID")
    void testFindById_NotFound() {
        Optional<Category> found = categoryService.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Получение всех категорий")
    void testFindAll() {
        Category category1 = new Category("Развлечения", CategoryType.SYSTEM, 1L);
        Category category2 = new Category("Образование", CategoryType.CUSTOM, 2L);

        categoryService.create(category1);
        categoryService.create(category2);

        List<Category> categories = categoryService.findAll();

        assertEquals(2, categories.size());
        assertTrue(categories.stream().anyMatch(c -> "Развлечения".equals(c.getName())));
        assertTrue(categories.stream().anyMatch(c -> "Образование".equals(c.getName())));
    }

    @Test
    @DisplayName("Обновление категории")
    void testUpdateCategory_Success() {
        Category category = new Category("Развлечения", CategoryType.SYSTEM, 1L);
        Category created = categoryService.create(category);

        created.setName("Развлечения и игры");
        created.setType(CategoryType.CUSTOM);

        Category updated = categoryService.update(created);

        assertEquals("Развлечения и игры", updated.getName());
        assertEquals(CategoryType.CUSTOM, updated.getType());
        assertEquals(created.getId(), updated.getId());
    }

    @Test
    @DisplayName("Обновление несуществующей категории")
    void testUpdateCategory_NotFound() {
        Category category = new Category("Тест", CategoryType.SYSTEM, 1L);
        category.setId(999L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> categoryService.update(category));

        assertTrue(exception.getMessage().contains("Category not found"));
    }

    @Test
    @DisplayName("Удаление категории")
    void testDeleteCategory_Success() {
        Category category = new Category("Образование", CategoryType.CUSTOM, 2L);
        Category created = categoryService.create(category);

        boolean deleted = categoryService.deleteById(created.getId());

        assertTrue(deleted);
        assertEquals(0, categoryService.getTotalCount());
        assertFalse(categoryService.findById(created.getId()).isPresent());
    }

    @Test
    @DisplayName("Удаление несуществующей категории")
    void testDeleteCategory_NotFound() {
        boolean deleted = categoryService.deleteById(999L);

        assertFalse(deleted);
    }

    @Test
    @DisplayName("Получение системных категорий")
    void testGetDefaultCategories() {
        Category default1 = new Category("Развлечения", CategoryType.SYSTEM, 1L);
        Category default2 = new Category("Образование", CategoryType.SYSTEM, 1L);
        Category user1 = new Category("Личное", CategoryType.CUSTOM, 2L);
        Category user2 = new Category("Работа", CategoryType.CUSTOM, 3L);

        categoryService.create(default1);
        categoryService.create(default2);
        categoryService.create(user1);
        categoryService.create(user2);

        List<Category> defaultCategories = categoryService.getDefaultCategories();

        assertEquals(2, defaultCategories.size());
        assertTrue(defaultCategories.stream().allMatch(c -> CategoryType.SYSTEM.equals(c.getType())));
        assertTrue(defaultCategories.stream().anyMatch(c -> "Развлечения".equals(c.getName())));
        assertTrue(defaultCategories.stream().anyMatch(c -> "Образование".equals(c.getName())));
    }

    @Test
    @DisplayName("Получение категорий пользователя")
    void testGetCategoriesByUser() {
        Category default1 = new Category("Развлечения", CategoryType.SYSTEM, 1L);
        Category user1_cat1 = new Category("Личное", CategoryType.CUSTOM, 2L);
        Category user1_cat2 = new Category("Хобби", CategoryType.CUSTOM, 2L);
        Category user2_cat1 = new Category("Работа", CategoryType.CUSTOM, 3L);

        categoryService.create(default1);
        categoryService.create(user1_cat1);
        categoryService.create(user1_cat2);
        categoryService.create(user2_cat1);

        List<Category> user2Categories = categoryService.getCategoriesByUser(2L);

        assertEquals(2, user2Categories.size());
        assertTrue(user2Categories.stream().allMatch(c -> c.getCreatedByUserId().equals(2L)));
        assertTrue(user2Categories.stream().anyMatch(c -> "Личное".equals(c.getName())));
        assertTrue(user2Categories.stream().anyMatch(c -> "Хобби".equals(c.getName())));
    }

    @Test
    @DisplayName("Получение доступных категорий для пользователя")
    void testGetAvailableCategoriesForUser() {
        Category default1 = new Category("Развлечения", CategoryType.SYSTEM, 1L);
        Category default2 = new Category("Образование", CategoryType.SYSTEM, 1L);
        Category user2_cat1 = new Category("Личное", CategoryType.CUSTOM, 2L);
        Category user3_cat1 = new Category("Работа", CategoryType.CUSTOM, 3L);

        categoryService.create(default1);
        categoryService.create(default2);
        categoryService.create(user2_cat1);
        categoryService.create(user3_cat1);

        List<Category> user2Available = categoryService.getAvailableCategoriesForUser(2L);

        assertEquals(3, user2Available.size());
        assertTrue(user2Available.stream().anyMatch(c -> "Развлечения".equals(c.getName())));
        assertTrue(user2Available.stream().anyMatch(c -> "Образование".equals(c.getName())));
        assertTrue(user2Available.stream().anyMatch(c -> "Личное".equals(c.getName())));
        assertFalse(user2Available.stream().anyMatch(c -> "Работа".equals(c.getName())));
    }

    @Test
    @DisplayName("Поиск категории по имени")
    void testFindByName_Success() {
        Category category = new Category("Развлечения", CategoryType.SYSTEM, 1L);
        categoryService.create(category);

        Optional<Category> found = categoryService.findByName("Развлечения");

        assertTrue(found.isPresent());
        assertEquals("Развлечения", found.get().getName());
    }

    @Test
    @DisplayName("Поиск категории по несуществующему имени")
    void testFindByName_NotFound() {
        Optional<Category> found = categoryService.findByName("Несуществующая");

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Поиск категории по null имени")
    void testFindByName_NullName() {
        Optional<Category> found = categoryService.findByName(null);

        assertFalse(found.isPresent());
    }
}