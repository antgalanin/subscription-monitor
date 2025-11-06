package com.subscriptionmonitor.controller;

import com.subscriptionmonitor.dto.CategoryResponse;
import com.subscriptionmonitor.dto.CreateCategoryRequest;
import com.subscriptionmonitor.dto.UpdateCategoryRequest;
import com.subscriptionmonitor.exception.notfound.CategoryNotFoundException;
import com.subscriptionmonitor.exception.validation.CategoryValidationException;
import com.subscriptionmonitor.exception.special.LegacyCategoryException;
import com.subscriptionmonitor.model.entity.Category;
import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.CategoryType;
import com.subscriptionmonitor.security.CategorySecurityService;
import com.subscriptionmonitor.service.CategoryService;
import com.subscriptionmonitor.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/api/categories", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Categories", description = "API для управления категориями подписок")
@SecurityRequirement(name = "basicAuth")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategorySecurityService categorySecurityService;
    private final UserService userService;

    @Operation(
            summary = "Создать новую категорию",
            description = "Создание новой категории. Доступ: ADMIN - системные и кастомные (для любого пользователя), USER - только свои кастомные."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category successfully created",
            content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Category name is required\",\"code\":\"CATEGORY_VALIDATION_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories\"}"))),
        @ApiResponse(responseCode = "410", description = "Cannot create LEGACY category",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":410,\"error\":\"Gone\",\"message\":\"Category with id 123e4567-e89b-12d3-a456-426614174000 is LEGACY and cannot be used\",\"code\":\"CATEGORY_LEGACY\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories\"}")))
    })
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) throws CategoryValidationException, LegacyCategoryException {
        User currentUser = categorySecurityService.getCurrentUser();
        Category category = toEntityFromCreateRequest(request, currentUser);
        Category created = categoryService.create(category, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @Operation(
            summary = "Получить категорию по ID",
            description = "Возвращает категорию по ее ID. Доступ: ADMIN - любые категории, USER - системные и свои кастомные."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/{id}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "Category not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Category with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"CATEGORY_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/{id}\"}")))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @categorySecurityService.isOwner(#id)")
    public ResponseEntity<CategoryResponse> getById(@Parameter(description = "Category ID") @PathVariable UUID id) throws CategoryNotFoundException {
        Category category = categoryService.findById(id);
        return ResponseEntity.ok(toResponse(category));
    }

    @Operation(
            summary = "Получить все категории",
            description = "Возвращает все доступные категории. Доступ: ADMIN - все категории, USER - все системные и свои кастомные."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories\"}")))
    })
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<CategoryResponse>> getAll() {
        User currentUser = categorySecurityService.getCurrentUser();
        List<CategoryResponse> categories;

        if (currentUser.getRole().name().equals("ADMIN")) {
            categories = categoryService.findAll().stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } else {
            categories = categoryService.findAll().stream()
                    .filter(category -> isAccessibleByUser(category, currentUser))
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "Получить категории по типу",
            description = "Возвращает все доступные категории по типу. Доступ: ADMIN - любые категории, USER - все системные, свои кастомные и свои устаревшие."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/type/{type}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/type/{type}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/type/{type}\"}")))
    })
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<CategoryResponse>> getByType(@Parameter(description = "Category type") @PathVariable CategoryType type) {
        User currentUser = categorySecurityService.getCurrentUser();
        List<CategoryResponse> categories;

        if (currentUser.getRole().name().equals("ADMIN")) {
            categories = categoryService.findByType(type).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } else {
            categories = categoryService.findByType(type).stream()
                    .filter(category -> isAccessibleByUser(category, currentUser))
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "Получить категории пользователя по ID",
            description = "Возвращает все категории пользователя по его ID. Доступ: ADMIN - категории любого пользователя, USER - свои кастомные и свои устаревшие."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/user/{userId}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access is denied\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/user/{userId}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/user/{userId}\"}")))
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#userId)")
    public ResponseEntity<List<CategoryResponse>> getByUserId(@Parameter(description = "User ID") @PathVariable UUID userId) {
        List<CategoryResponse> categories = categoryService.findByCreatedByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "Обновить категорию",
            description = "Обновляет категорию. Доступ: ADMIN - системные и кастомные (для любого пользователя), USER - свои кастомные."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Category name is required\",\"code\":\"CATEGORY_VALIDATION_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/{id}\"}"))),
        @ApiResponse(responseCode = "403", description = "Access denied - Users can only modify their own CUSTOM categories",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Users can only modify their own categories\",\"code\":\"ACCESS_FORBIDDEN\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "Category not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Category with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"CATEGORY_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/{id}\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/{id}\"}"))),
        @ApiResponse(responseCode = "410", description = "Cannot update LEGACY category",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":410,\"error\":\"Gone\",\"message\":\"Category with id 123e4567-e89b-12d3-a456-426614174000 is LEGACY and cannot be used\",\"code\":\"CATEGORY_LEGACY\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/{id}\"}")))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @categorySecurityService.isOwner(#id)")
    public ResponseEntity<CategoryResponse> update(@Parameter(description = "Category ID") @PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest request) throws CategoryNotFoundException, CategoryValidationException, LegacyCategoryException {
        User currentUser = categorySecurityService.getCurrentUser();
        Category existing = categoryService.findById(id);
        Category category = toEntityFromUpdateRequest(id, request, existing);
        Category updated = categoryService.update(category, currentUser);
        return ResponseEntity.ok(toResponse(updated));
    }

    @Operation(
            summary = "Удалить категорию по ID",
            description = "Удаляет категорию по ее ID. Устаревшие категории удалять нельзя. Доступ: ADMIN - системные категории и свои кастомные, USER - свои кастомные."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You do not have permission to access this resource\",\"code\":\"ACCESS_FORBIDDEN\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/{id}\"}"))),
        @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/{id}\"}"))),
        @ApiResponse(responseCode = "404", description = "Category not found",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Category with id 123e4567-e89b-12d3-a456-426614174000 not found\",\"code\":\"CATEGORY_NOT_FOUND\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/{id}\"}"))),
        @ApiResponse(responseCode = "410", description = "Cannot delete LEGACY category",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":410,\"error\":\"Gone\",\"message\":\"Category with id 123e4567-e89b-12d3-a456-426614174000 is LEGACY and cannot be deleted\",\"code\":\"CATEGORY_LEGACY\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/{id}\"}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = com.subscriptionmonitor.dto.ErrorResponse.class),
                examples = @ExampleObject(value = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"code\":\"INTERNAL_ERROR\",\"timestamp\":\"2025-10-19T18:30:00\",\"path\":\"/api/categories/{id}\"}")))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @categorySecurityService.isOwner(#id)")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Category ID") @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        categoryService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    private boolean isAccessibleByUser(Category category, User user) {
        return category.getType() == CategoryType.SYSTEM ||
               (category.getType() == CategoryType.LEGACY && category.getCreatedByUserId() == null) ||
               (category.getCreatedByUserId() != null && category.getCreatedByUserId().equals(user.getId()));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getCreatedByUserId(),
                category.getCreatedAt()
        );
    }

    private Category toEntityFromCreateRequest(CreateCategoryRequest request, User currentUser) {
        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        return category;
    }

    private Category toEntityFromUpdateRequest(UUID id, UpdateCategoryRequest request, Category existing) {
        Category category = new Category();
        category.setId(id);
        category.setName(request.getName() != null ? request.getName() : existing.getName());
        category.setType(request.getType() != null ? request.getType() : existing.getType());
        category.setCreatedByUserId(existing.getCreatedByUserId());
        category.setCreatedAt(existing.getCreatedAt());
        return category;
    }
}
