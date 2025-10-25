package com.subscriptionmonitor.security;

import com.subscriptionmonitor.model.entity.Notification;
import com.subscriptionmonitor.model.entity.Subscription;
import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.repository.NotificationRepository;
import com.subscriptionmonitor.repository.SubscriptionRepository;
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
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;

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

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean isSubscriptionOwner(UUID subscriptionId) {
        UUID currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }

        return subscriptionRepository.findById(subscriptionId)
                .map(subscription -> subscription.getUserId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isNotificationOwner(UUID notificationId) {
        UUID currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }

        return notificationRepository.findById(notificationId)
                .map(notification -> notification.getUserId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isPaymentOwner(UUID paymentId) {
        UUID currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }

        return subscriptionRepository.findAll().stream()
                .filter(sub -> sub.getPayment() != null && sub.getPayment().getId().equals(paymentId))
                .anyMatch(sub -> sub.getUserId().equals(currentUserId));
    }

    public java.util.Set<UUID> getPaymentIdsForCurrentUser() {
        UUID currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return java.util.Collections.emptySet();
        }

        return subscriptionRepository.findAll().stream()
                .filter(sub -> sub.getUserId().equals(currentUserId) && sub.getPayment() != null)
                .map(sub -> sub.getPayment().getId())
                .collect(java.util.stream.Collectors.toSet());
    }
}
