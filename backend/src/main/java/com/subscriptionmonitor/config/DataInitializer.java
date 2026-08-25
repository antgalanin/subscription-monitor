package com.subscriptionmonitor.config;

import com.subscriptionmonitor.exception.validation.CategoryValidationException;
import com.subscriptionmonitor.exception.validation.UserValidationException;
import com.subscriptionmonitor.exception.special.LegacyCategoryException;
import com.subscriptionmonitor.model.entity.Category;
import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.CategoryType;
import com.subscriptionmonitor.model.enums.UserRole;
import com.subscriptionmonitor.repository.CategoryRepository;
import com.subscriptionmonitor.repository.UserRepository;
import com.subscriptionmonitor.service.CategoryService;
import com.subscriptionmonitor.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    @Value("${app.init.admin-password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        log.info("Starting data initialization...");

        initializeUsers();
        initializeCategories();

        log.info("Data initialization completed successfully");
    }

    private void initializeUsers() {
        try {
            if (userRepository.findByUsername("admin").isPresent()) {
                log.info("Admin user already exists, skipping creation");
                return;
            }
            if (adminPassword == null || adminPassword.isBlank()) {
                log.warn("Admin user not created: app.init.admin-password is not set");
                return;
            }
            User admin = new User(
                    "admin",
                    "admin@subscriptionmonitor.com",
                    adminPassword,
                    UserRole.ADMIN,
                    3
            );
            userService.create(admin, null);
            log.info("Admin user created successfully");
        } catch (UserValidationException e) {
            log.error("Error creating admin user: {}", e.getMessage());
        }
    }

    private void initializeCategories() {
        String[] systemCategories = {
                "Streaming",
                "AI Tools",
                "Cloud Storage",
                "Gaming",
                "Education",
                "Fitness",
                "Productivity",
                "News & Media",
                "Music",
                "Development Tools",
                "VPN"
        };

        for (String categoryName : systemCategories) {
            try {
                if (categoryRepository.findByNameAndType(categoryName, CategoryType.SYSTEM).isEmpty()) {
                    Category category = new Category(categoryName, CategoryType.SYSTEM, null);
                    categoryService.create(category, null);
                    log.info("System category '{}' created successfully", categoryName);
                } else {
                    log.debug("System category '{}' already exists, skipping", categoryName);
                }
            } catch (CategoryValidationException | LegacyCategoryException e) {
                log.error("Error creating system category '{}': {}", categoryName, e.getMessage());
            }
        }
    }
}
