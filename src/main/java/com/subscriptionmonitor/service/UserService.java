package com.subscriptionmonitor.service;

import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.UserRole;
import com.subscriptionmonitor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public User create(User user) {
        log.debug("Creating user: {}", user.getUsername());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public User update(User user) {
        log.debug("Updating user: {}", user.getId());
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        log.debug("Deleting user: {}", id);
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long getTotalCount() {
        return userRepository.count();
    }
}