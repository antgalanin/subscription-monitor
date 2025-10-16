package com.subscriptionmonitor.security;

import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;

    public boolean isOwner(UUID userId) {
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

        return currentUser != null && currentUser.getId().equals(userId);
    }

    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return null;
        }

        UserDetails userDetails = (UserDetails) principal;
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElse(null);

        return currentUser != null ? currentUser.getId() : null;
    }
}
