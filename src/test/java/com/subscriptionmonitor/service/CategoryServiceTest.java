package com.subscriptionmonitor.service;

import com.subscriptionmonitor.exception.notfound.CategoryNotFoundException;
import com.subscriptionmonitor.model.entity.Category;
import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.CategoryType;
import com.subscriptionmonitor.model.enums.UserRole;
import com.subscriptionmonitor.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private UUID testCategoryId;
    private UUID userId;
    private UUID category1Id;
    private UUID category2Id;
    private User mockAdminUser;
    private User mockRegularUser;

    @BeforeEach
    void setUp() {
        testCategoryId = UUID.randomUUID();
        userId = UUID.randomUUID();
        category1Id = UUID.randomUUID();
        category2Id = UUID.randomUUID();

        mockAdminUser = new User("admin", "admin@test.com", "password", UserRole.ADMIN, 5);
        mockAdminUser.setId(userId);

        mockRegularUser = new User("user", "user@test.com", "password", UserRole.USER, 5);
        mockRegularUser.setId(UUID.randomUUID());

        testCategory = new Category("Streaming", CategoryType.SYSTEM, userId);
        testCategory.setId(testCategoryId);
    }

    @Test
    @DisplayName("Создание категории с корректными данными")
    void testCreateCategory_Success() throws Exception {
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        Category created = categoryService.create(testCategory, mockAdminUser);

        assertNotNull(created);
        assertEquals(testCategoryId, created.getId());
        assertEquals("Streaming", created.getName());
        assertEquals(CategoryType.SYSTEM, created.getType());

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Поиск категории по ID")
    void testFindById_Success() throws Exception {
        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.of(testCategory));

        Category found = categoryService.findById(testCategoryId);

        assertNotNull(found);
        assertEquals(testCategoryId, found.getId());
        assertEquals("Streaming", found.getName());

        verify(categoryRepository, times(1)).findById(testCategoryId);
    }

    @Test
    @DisplayName("Поиск категории по несуществующему ID")
    void testFindById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.findById(nonExistentId);
        });

        verify(categoryRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Получение всех категорий")
    void testFindAll() {
        Category category1 = new Category("Streaming", CategoryType.SYSTEM, userId);
        category1.setId(category1Id);
        Category category2 = new Category("Software", CategoryType.CUSTOM, userId);
        category2.setId(category2Id);

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2));

        List<Category> categories = categoryService.findAll();

        assertEquals(2, categories.size());
        assertTrue(categories.stream().anyMatch(c -> "Streaming".equals(c.getName())));
        assertTrue(categories.stream().anyMatch(c -> "Software".equals(c.getName())));

        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Поиск категорий по типу")
    void testFindByType() {
        Category category1 = new Category("Netflix", CategoryType.SYSTEM, userId);
        category1.setId(category1Id);
        Category category2 = new Category("Spotify", CategoryType.SYSTEM, userId);
        category2.setId(category2Id);

        when(categoryRepository.findByType(CategoryType.SYSTEM))
                .thenReturn(Arrays.asList(category1, category2));

        List<Category> categories = categoryService.findByType(CategoryType.SYSTEM);

        assertEquals(2, categories.size());
        assertTrue(categories.stream().allMatch(c -> c.getType() == CategoryType.SYSTEM));

        verify(categoryRepository, times(1)).findByType(CategoryType.SYSTEM);
    }

    @Test
    @DisplayName("Поиск категорий по пользователю")
    void testFindByCreatedByUserId() {
        Category category1 = new Category("Category1", CategoryType.CUSTOM, userId);
        category1.setId(category1Id);
        Category category2 = new Category("Category2", CategoryType.CUSTOM, userId);
        category2.setId(category2Id);

        when(categoryRepository.findByCreatedByUserId(userId))
                .thenReturn(Arrays.asList(category1, category2));

        List<Category> categories = categoryService.findByCreatedByUserId(userId);

        assertEquals(2, categories.size());
        assertTrue(categories.stream().allMatch(c -> c.getCreatedByUserId().equals(userId)));

        verify(categoryRepository, times(1)).findByCreatedByUserId(userId);
    }

    @Test
    @DisplayName("Поиск категорий по типу и пользователю")
    void testFindByTypeAndCreatedByUserId() {
        Category category1 = new Category("Custom1", CategoryType.CUSTOM, userId);
        category1.setId(category1Id);

        when(categoryRepository.findByTypeAndCreatedByUserId(CategoryType.CUSTOM, userId))
                .thenReturn(Arrays.asList(category1));

        List<Category> categories = categoryService.findByTypeAndCreatedByUserId(CategoryType.CUSTOM, userId);

        assertEquals(1, categories.size());
        assertEquals(CategoryType.CUSTOM, categories.get(0).getType());
        assertEquals(userId, categories.get(0).getCreatedByUserId());

        verify(categoryRepository, times(1)).findByTypeAndCreatedByUserId(CategoryType.CUSTOM, userId);
    }

    @Test
    @DisplayName("Обновление категории")
    void testUpdateCategory_Success() throws Exception {
        Category updatedCategory = new Category("Updated Name", CategoryType.SYSTEM, userId);
        updatedCategory.setId(testCategoryId);

        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        Category updated = categoryService.update(updatedCategory, mockAdminUser);

        assertEquals("Updated Name", updated.getName());
        assertEquals(testCategoryId, updated.getId());

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Удаление категории")
    void testDeleteCategory_Success() throws Exception {
        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).deleteById(testCategoryId);

        categoryService.delete(testCategoryId, mockAdminUser);

        verify(categoryRepository, times(1)).deleteById(testCategoryId);
    }

    @Test
    @DisplayName("Удаление всех категорий")
    void testDeleteAll() {
        doNothing().when(categoryRepository).deleteAll();

        categoryService.deleteAll();

        verify(categoryRepository, times(1)).deleteAll();
    }
}
