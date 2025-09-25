package com.subscriptionmonitor.model.entity;

import com.subscriptionmonitor.model.enums.UserRole;

public class User extends BaseEntity {
    private String username;
    private String email;
    private String password;
    private UserRole role;
    private Integer notificationDays;

    public User() {
        super();
        this.role = UserRole.USER;
        this.notificationDays = 3;
    }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Integer getNotificationDays() {
        return notificationDays;
    }

    public void setNotificationDays(Integer notificationDays) {
        this.notificationDays = notificationDays;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", uuid=" + getUuid() +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", notificationDays=" + notificationDays +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}