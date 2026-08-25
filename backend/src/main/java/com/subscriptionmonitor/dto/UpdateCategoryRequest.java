package com.subscriptionmonitor.dto;

import com.subscriptionmonitor.model.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на обновление категории")
public class UpdateCategoryRequest {

    @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
    @Schema(description = "Название категории", example = "Развлечения")
    private String name;

    @Schema(description = "Тип категории (только для ADMIN)", example = "CUSTOM", allowableValues = {"SYSTEM", "CUSTOM", "LEGACY"})
    private CategoryType type;
}
