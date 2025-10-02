package com.subscriptionmonitor.model.entity;

import com.subscriptionmonitor.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Column(name = "notification_days", nullable = false)
    private Integer notificationDays = 3;

    public User(String username, String email, String password) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = UserRole.USER;
        this.notificationDays = 3;
    }

    public User(String username, String email, String password, UserRole role, Integer notificationDays) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.notificationDays = notificationDays;
    }
}
