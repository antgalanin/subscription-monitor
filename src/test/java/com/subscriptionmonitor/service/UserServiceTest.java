package com.subscriptionmonitor.service;

import com.subscriptionmonitor.exception.UserNotFoundException;
import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.UserRole;
import com.subscriptionmonitor.repository.UserRepository;
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
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID testUserId;
    private UUID user1Id;
    private UUID user2Id;
    private UUID user3Id;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        user1Id = UUID.randomUUID();
        user2Id = UUID.randomUUID();
        user3Id = UUID.randomUUID();

        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(testUserId);
    }

    @Test
    @DisplayName("Создание пользователя с корректными данными")
    void testCreateUser_Success() throws Exception {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User created = userService.create(testUser);

        assertNotNull(created);
        assertEquals(testUserId, created.getId());
        assertEquals("testuser", created.getUsername());
        assertEquals("test@example.com", created.getEmail());
        assertEquals(UserRole.USER, created.getRole());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Поиск пользователя по ID")
    void testFindById_Success() throws Exception {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        User found = userService.findById(testUserId);

        assertNotNull(found);
        assertEquals(testUserId, found.getId());
        assertEquals("testuser", found.getUsername());

        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("Поиск пользователя по несуществующему ID")
    void testFindById_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.findById(nonExistentId);
        });

        verify(userRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Поиск пользователя по username")
    void testFindByUsername_Success() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User found = userService.findByUsername("testuser");

        assertNotNull(found);
        assertEquals("testuser", found.getUsername());

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Поиск пользователя по несуществующему username")
    void testFindByUsername_NotFound() throws Exception {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.findByUsername("nonexistent");
        });

        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("Поиск пользователя по email")
    void testFindByEmail_Success() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User found = userService.findByEmail("test@example.com");

        assertNotNull(found);
        assertEquals("test@example.com", found.getEmail());

        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void testFindAll() {
        User user1 = new User("user1", "user1@example.com", "password123");
        user1.setId(user1Id);
        User user2 = new User("user2", "user2@example.com", "password456");
        user2.setId(user2Id);

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = userService.findAll();

        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> "user1".equals(u.getUsername())));
        assertTrue(users.stream().anyMatch(u -> "user2".equals(u.getUsername())));

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Обновление пользователя")
    void testUpdateUser_Success() throws Exception {
        User updatedUser = new User("testuser", "newemail@example.com", "password123");
        updatedUser.setId(testUserId);
        updatedUser.setNotificationDays(7);

        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User updated = userService.update(updatedUser);

        assertEquals("newemail@example.com", updated.getEmail());
        assertEquals(7, updated.getNotificationDays());
        assertEquals(testUserId, updated.getId());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Удаление пользователя")
    void testDeleteUser_Success() throws Exception {
        when(userRepository.existsById(testUserId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(testUserId);

        userService.delete(testUserId);

        verify(userRepository, times(1)).deleteById(testUserId);
    }

    @Test
    @DisplayName("Поиск пользователей по роли")
    void testFindByRole() {
        User admin = new User("admin", "admin@example.com", "password123", UserRole.ADMIN, 5);
        admin.setId(user1Id);

        User user1 = new User("user1", "user1@example.com", "password123");
        user1.setId(user2Id);

        User user2 = new User("user2", "user2@example.com", "password456");
        user2.setId(user3Id);

        when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(Arrays.asList(admin));
        when(userRepository.findByRole(UserRole.USER)).thenReturn(Arrays.asList(user1, user2));

        List<User> admins = userService.findByRole(UserRole.ADMIN);
        List<User> users = userService.findByRole(UserRole.USER);

        assertEquals(1, admins.size());
        assertEquals(2, users.size());
        assertEquals("admin", admins.get(0).getUsername());

        verify(userRepository, times(1)).findByRole(UserRole.ADMIN);
        verify(userRepository, times(1)).findByRole(UserRole.USER);
    }

    @Test
    @DisplayName("Получение общего количества пользователей")
    void testGetTotalCount() {
        when(userRepository.count()).thenReturn(5L);

        long count = userService.getTotalCount();

        assertEquals(5L, count);

        verify(userRepository, times(1)).count();
    }
}
