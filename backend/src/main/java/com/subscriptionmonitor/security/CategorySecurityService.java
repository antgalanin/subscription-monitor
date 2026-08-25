package com.subscriptionmonitor.security;

import com.subscriptionmonitor.model.entity.Category;
import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.CategoryType;
import com.subscriptionmonitor.repository.CategoryRepository;
import com.subscriptionmonitor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategorySecurityService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public boolean isOwner(UUID categoryId) {
        User currentUser = getCurrentUser();
        Category category = findCategory(categoryId);
        return currentUser != null && category != null && isOwnedBy(category, currentUser);
    }

    public boolean canRead(UUID categoryId) {
        User currentUser = getCurrentUser();
        Category category = findCategory(categoryId);
        if (currentUser == null || category == null) {
            return false;
        }
        return category.getType() == CategoryType.SYSTEM || isOwnedBy(category, currentUser);
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return null;
        }

        UserDetails userDetails = (UserDetails) principal;
        return userRepository.findByUsername(userDetails.getUsername()).orElse(null);
    }

    private Category findCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId).orElse(null);
    }

    private boolean isOwnedBy(Category category, User user) {
        return category.getType() != CategoryType.SYSTEM
                && category.getCreatedByUserId() != null
                && category.getCreatedByUserId().equals(user.getId());
    }
}
