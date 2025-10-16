package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.UserDto;
import com.subscriptionmonitor.exception.notfound.UserNotFoundException;
import com.subscriptionmonitor.exception.validation.UserValidationException;
import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.UserRole;
import com.subscriptionmonitor.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody UserDto userDto) throws UserValidationException {
        User user = toEntity(userDto);
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
    public ResponseEntity<UserDto> getById(@PathVariable UUID id) throws UserNotFoundException {
        User user = userService.findById(id);
        return ResponseEntity.ok(toDto(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAll() {
        List<UserDto> users = userService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getByUsername(@PathVariable String username) throws UserNotFoundException {
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(toDto(user));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getByEmail(@PathVariable String email) throws UserNotFoundException {
        User user = userService.findByEmail(email);
        return ResponseEntity.ok(toDto(user));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getByRole(@PathVariable UserRole role) {
        List<UserDto> users = userService.findByRole(role).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
    public ResponseEntity<UserDto> update(@PathVariable UUID id, @RequestBody UserDto userDto) throws UserNotFoundException, UserValidationException {
        userDto.setId(id);
        User user = toEntity(userDto);
        User updated = userService.update(user);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) throws UserNotFoundException {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null, // Never return password in DTO
                user.getRole(),
                user.getNotificationDays(),
                user.getCreatedAt()
        );
    }

    private User toEntity(UserDto dto) {
        User user = new User();
        if (dto.getId() != null) {
            user.setId(dto.getId());
        }
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getNotificationDays() != null) {
            user.setNotificationDays(dto.getNotificationDays());
        }
        if (dto.getCreatedAt() != null) {
            user.setCreatedAt(dto.getCreatedAt());
        }
        return user;
    }
}
