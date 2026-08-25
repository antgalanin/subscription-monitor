package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными пользователя")
public class UserResponse {

    @Schema(description = "Уникальный идентификатор пользователя", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Имя пользователя для входа в систему", example = "john_doe")
    private String username;

    @Schema(description = "Email адрес пользователя", example = "john@example.com")
    private String email;

    @Schema(description = "Роль пользователя в системе", example = "USER", allowableValues = {"USER", "ADMIN"})
    private UserRole role;

    @Schema(description = "За сколько дней до списания отправлять уведомления", example = "3")
    private Integer notificationDays;

    @Schema(description = "Дата и время создания пользователя", example = "2025-10-20T15:30:00")
    private LocalDateTime createdAt;
}
