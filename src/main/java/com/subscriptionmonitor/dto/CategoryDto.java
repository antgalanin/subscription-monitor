package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private UUID id;
    private String name;
    private CategoryType type;
    private UUID createdByUserId;
    private LocalDateTime createdAt;
}
