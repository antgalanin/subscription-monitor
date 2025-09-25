package com.subscriptionmonitor.service;

import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.UserRole;
import com.subscriptionmonitor.storage.DataStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserService Tests")
class UserServiceTest {

    private UserService userService;
    private DataStorage storage;

    @BeforeEach
    void setUp() {
        storage = DataStorage.getInstance();
        storage.clearAll();
        userService = new UserService();
    }

    @Test
    @DisplayName("Создание пользователя с корректными данными")
    void testCreateUser_Success() {
        User user = new User("testuser", "test@example.com", "password123");

        User created = userService.create(user);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertNotNull(created.getUuid());
        assertEquals("testuser", created.getUsername());
        assertEquals("test@example.com", created.getEmail());
        assertEquals(UserRole.USER, created.getRole());
        assertEquals(1, userService.getTotalCount());
    }

    @Test
    @DisplayName("Создание пользователя с дублирующимся username")
    void testCreateUser_DuplicateUsername() {
        User user1 = new User("testuser", "test1@example.com", "password123");
        User user2 = new User("testuser", "test2@example.com", "password456");

        userService.create(user1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.create(user2));

        assertTrue(exception.getMessage().contains("Username already exists"));
        assertEquals(1, userService.getTotalCount());
    }

    @Test
    @DisplayName("Создание пользователя с дублирующимся email")
    void testCreateUser_DuplicateEmail() {
        User user1 = new User("testuser1", "test@example.com", "password123");
        User user2 = new User("testuser2", "test@example.com", "password456");

        userService.create(user1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.create(user2));

        assertTrue(exception.getMessage().contains("Email already exists"));
        assertEquals(1, userService.getTotalCount());
    }

    @Test
    @DisplayName("Создание пользователя с null")
    void testCreateUser_Null() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.create(null));

        assertEquals("User cannot be null", exception.getMessage());
        assertEquals(0, userService.getTotalCount());
    }

    @Test
    @DisplayName("Поиск пользователя по ID")
    void testFindById_Success() {
        User user = new User("testuser", "test@example.com", "password123");
        User created = userService.create(user);

        Optional<User> found = userService.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    @DisplayName("Поиск пользователя по несуществующему ID")
    void testFindById_NotFound() {
        Optional<User> found = userService.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Поиск пользователя по null ID")
    void testFindById_NullId() {
        Optional<User> found = userService.findById(null);

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Поиск пользователя по username")
    void testFindByUsername_Success() {
        User user = new User("testuser", "test@example.com", "password123");
        userService.create(user);

        Optional<User> found = userService.findByUsername("testuser");

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    @DisplayName("Поиск пользователя по несуществующему username")
    void testFindByUsername_NotFound() {
        Optional<User> found = userService.findByUsername("nonexistent");

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Поиск пользователя по email")
    void testFindByEmail_Success() {
        User user = new User("testuser", "test@example.com", "password123");
        userService.create(user);

        Optional<User> found = userService.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void testFindAll() {
        User user1 = new User("user1", "user1@example.com", "password123");
        User user2 = new User("user2", "user2@example.com", "password456");

        userService.create(user1);
        userService.create(user2);

        List<User> users = userService.findAll();

        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> "user1".equals(u.getUsername())));
        assertTrue(users.stream().anyMatch(u -> "user2".equals(u.getUsername())));
    }

    @Test
    @DisplayName("Обновление пользователя")
    void testUpdateUser_Success() {
        User user = new User("testuser", "test@example.com", "password123");
        User created = userService.create(user);

        created.setEmail("newemail@example.com");
        created.setNotificationDays(7);

        User updated = userService.update(created);

        assertEquals("newemail@example.com", updated.getEmail());
        assertEquals(7, updated.getNotificationDays());
        assertEquals(created.getId(), updated.getId());
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя")
    void testUpdateUser_NotFound() {
        User user = new User("testuser", "test@example.com", "password123");
        user.setId(999L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.update(user));

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    @DisplayName("Удаление пользователя")
    void testDeleteUser_Success() {
        User user = new User("testuser", "test@example.com", "password123");
        User created = userService.create(user);

        boolean deleted = userService.deleteById(created.getId());

        assertTrue(deleted);
        assertEquals(0, userService.getTotalCount());
        assertFalse(userService.findById(created.getId()).isPresent());
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя")
    void testDeleteUser_NotFound() {
        boolean deleted = userService.deleteById(999L);

        assertFalse(deleted);
    }

    @Test
    @DisplayName("Поиск пользователей по роли")
    void testFindByRole() {
        User admin = new User("admin", "admin@example.com", "password123", UserRole.ADMIN, 5);
        User user1 = new User("user1", "user1@example.com", "password123");
        User user2 = new User("user2", "user2@example.com", "password456");

        userService.create(admin);
        userService.create(user1);
        userService.create(user2);

        List<User> admins = userService.findByRole(UserRole.ADMIN);
        List<User> users = userService.findByRole(UserRole.USER);

        assertEquals(1, admins.size());
        assertEquals(2, users.size());
        assertEquals("admin", admins.get(0).getUsername());
    }
}