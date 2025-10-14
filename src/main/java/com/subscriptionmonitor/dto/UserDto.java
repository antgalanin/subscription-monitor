package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String password;
    private UserRole role;
    private Integer notificationDays;
    private LocalDateTime createdAt;
}
