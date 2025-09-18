package com.subscriptionmonitor.service;

import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.UserRole;
import com.subscriptionmonitor.storage.DataStorage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserService implements CrudService<User, Long> {
    private final DataStorage storage;

    public UserService() {
        this.storage = DataStorage.getInstance();
    }

    @Override
    public User create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        if (findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        Long id = storage.generateUserId();
        user.setId(id);
        storage.getUsers().put(id, user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.getUsers().get(id));
    }

    @Override
    public List<User> findAll() {
        return storage.getUsers().values().stream().collect(Collectors.toList());
    }

    @Override
    public User update(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and user ID cannot be null");
        }

        if (!storage.getUsers().containsKey(user.getId())) {
            throw new IllegalArgumentException("User not found with ID: " + user.getId());
        }

        storage.getUsers().put(user.getId(), user);
        return user;
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        return storage.getUsers().remove(id) != null;
    }

    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }

        return storage.getUsers().values().stream()
                .filter(user -> username.equals(user.getUsername()))
                .findFirst();
    }

    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }

        return storage.getUsers().values().stream()
                .filter(user -> email.equals(user.getEmail()))
                .findFirst();
    }

    public List<User> findByRole(UserRole role) {
        if (role == null) {
            return Collections.emptyList();
        }

        return storage.getUsers().values().stream()
                .filter(user -> role.equals(user.getRole()))
                .collect(Collectors.toList());
    }

    public int getTotalCount() {
        return storage.getTotalUsersCount();
    }
}