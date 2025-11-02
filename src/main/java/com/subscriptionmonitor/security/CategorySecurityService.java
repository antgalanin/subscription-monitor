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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return false;
        }

        UserDetails userDetails = (UserDetails) principal;
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElse(null);

        if (currentUser == null) {
            return false;
        }

        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            return false;
        }

        if (category.getType() == CategoryType.SYSTEM) {
            return true;
        }

        return category.getCreatedByUserId() != null &&
                category.getCreatedByUserId().equals(currentUser.getId());
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
}