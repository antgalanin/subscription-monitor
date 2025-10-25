package com.subscriptionmonitor.service;

import com.subscriptionmonitor.exception.notfound.UserNotFoundException;
import com.subscriptionmonitor.exception.validation.UserValidationException;
import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.UserRole;
import com.subscriptionmonitor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(User user) throws UserValidationException {
        log.info("Registering new user with username: {}", user.getUsername());
        validateUser(user);
        validateUniqueUsername(user.getUsername(), null);
        validateUniqueEmail(user.getEmail(), null);

        user.setRole(UserRole.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User created = userRepository.save(user);
        log.info("User registered successfully with id: {}", created.getId());
        return created;
    }

    public User create(User user, User currentUser) throws UserValidationException {
        log.info("Creating user with username: {} by user: {}", user.getUsername(),
            currentUser != null ? currentUser.getUsername() : "system");
        validateUser(user);
        validateUniqueUsername(user.getUsername(), null);
        validateUniqueEmail(user.getEmail(), null);

        if (currentUser != null && currentUser.getRole() != UserRole.ADMIN) {
            user.setRole(UserRole.USER);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User created = userRepository.save(user);
        log.info("User created successfully with id: {}", created.getId());
        return created;
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) throws UserNotFoundException {
        log.debug("Finding user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) throws UserNotFoundException {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) throws UserNotFoundException {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public User update(User user, User currentUser) throws UserNotFoundException, UserValidationException {
        log.info("Updating user with id: {} by user: {}", user.getId(),
            currentUser != null ? currentUser.getUsername() : "system");

        if (user.getId() == null) {
            throw new UserValidationException("User ID cannot be null for update operation");
        }

        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new UserNotFoundException(user.getId()));

        validateUser(user);
        validateUniqueUsername(user.getUsername(), user.getId());
        validateUniqueEmail(user.getEmail(), user.getId());

        if (currentUser != null && currentUser.getRole() != UserRole.ADMIN) {
            if (!existing.getRole().equals(user.getRole())) {
                throw new AccessDeniedException("Users cannot change their own role");
            }
        }

        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        User updated = userRepository.save(user);
        log.info("User updated successfully: {}", updated.getId());
        return updated;
    }

    public void delete(UUID id) throws UserNotFoundException {
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }

    @Transactional(readOnly = true)
    public long getTotalCount() {
        return userRepository.count();
    }

    private void validateUser(User user) throws UserValidationException {
        if (user == null) {
            throw new UserValidationException("User cannot be null");
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new UserValidationException("Username cannot be empty");
        }
        if (user.getUsername().length() > 50) {
            throw new UserValidationException("Username cannot exceed 50 characters");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new UserValidationException("Email cannot be empty");
        }
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new UserValidationException("Invalid email format");
        }
        if (user.getEmail().length() > 100) {
            throw new UserValidationException("Email cannot exceed 100 characters");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new UserValidationException("Password cannot be empty");
        }
        if (user.getPassword().length() > 255) {
            throw new UserValidationException("Password cannot exceed 255 characters");
        }
        if (user.getNotificationDays() == null || user.getNotificationDays() < 0) {
            throw new UserValidationException("Notification days must be a positive number");
        }
    }

    private void validateUniqueUsername(String username, UUID excludeUserId) throws UserValidationException {
        var existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            if (excludeUserId == null || !existingUser.get().getId().equals(excludeUserId)) {
                throw new UserValidationException(
                        String.format("Username '%s' is already taken", username));
            }
        }
    }

    private void validateUniqueEmail(String email, UUID excludeUserId) throws UserValidationException {
        var existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            if (excludeUserId == null || !existingUser.get().getId().equals(excludeUserId)) {
                throw new UserValidationException(
                        String.format("Email '%s' is already registered", email));
            }
        }
    }
}