package com.subscriptionmonitor.repository;

import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с пользователями.
 *
 * @author Галанин А.Н.
 * @version 2.0 (ЛР2)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Найти пользователя по username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Найти пользователя по email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Найти всех пользователей с указанной ролью.
     */
    List<User> findByRole(UserRole role);
}
