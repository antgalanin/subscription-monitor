package com.subscriptionmonitor.gui.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.subscriptionmonitor.dto.*;
import com.subscriptionmonitor.model.enums.UserRole;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class RestClient {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String authHeader;
    private UserRole currentUserRole;
    private UUID currentUserId;
    private String currentUsername;

    public RestClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public boolean login(String username, String password) throws IOException, InterruptedException {
        String credentials = username + ":" + password;
        this.authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

        try {
            UserDto currentUser = getCurrentUser();
            if (currentUser != null) {
                this.currentUserRole = currentUser.getRole();
                this.currentUserId = currentUser.getId();
                this.currentUsername = currentUser.getUsername();
                return true;
            }
        } catch (Exception e) {
            this.authHeader = null;
            throw e;
        }
        return false;
    }

    private UserDto getCurrentUser() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/me"))
                .header("Authorization", authHeader)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), UserDto.class);
        }
        throw new IOException("Failed to get current user: " + response.statusCode());
    }

    public void logout() {
        this.authHeader = null;
        this.currentUserRole = null;
        this.currentUserId = null;
        this.currentUsername = null;
    }

    public boolean isLoggedIn() {
        return authHeader != null;
    }

    public UserRole getCurrentUserRole() {
        return currentUserRole;
    }

    public UUID getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public boolean isAdmin() {
        return currentUserRole == UserRole.ADMIN;
    }

    private HttpRequest.Builder createRequestBuilder(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json");
    }

    public <T> T get(String endpoint, Class<T> responseType) throws IOException, InterruptedException {
        HttpRequest request = createRequestBuilder(endpoint)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), responseType);
        }
        throw new IOException("Request failed: " + response.statusCode() + " - " + response.body());
    }

    public <T> List<T> getList(String endpoint, TypeReference<List<T>> typeReference) throws IOException, InterruptedException {
        HttpRequest request = createRequestBuilder(endpoint)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), typeReference);
        }
        throw new IOException("Request failed: " + response.statusCode() + " - " + response.body());
    }

    public <T> T post(String endpoint, Object body, Class<T> responseType) throws IOException, InterruptedException {
        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest request = createRequestBuilder(endpoint)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), responseType);
        }
        throw new IOException("Request failed: " + response.statusCode() + " - " + response.body());
    }

    public <T> T put(String endpoint, Object body, Class<T> responseType) throws IOException, InterruptedException {
        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest request = createRequestBuilder(endpoint)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), responseType);
        }
        throw new IOException("Request failed: " + response.statusCode() + " - " + response.body());
    }

    public void delete(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = createRequestBuilder(endpoint)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Request failed: " + response.statusCode() + " - " + response.body());
        }
    }

    public List<UserDto> getAllUsers() throws IOException, InterruptedException {
        return getList("/api/users", new TypeReference<List<UserDto>>() {});
    }

    public UserDto getUserById(UUID id) throws IOException, InterruptedException {
        return get("/api/users/" + id, UserDto.class);
    }

    public UserDto createUser(UserDto user) throws IOException, InterruptedException {
        return registerUser(user);
    }

    public UserDto registerUser(UserDto user) throws IOException, InterruptedException {
        String jsonBody = objectMapper.writeValueAsString(user);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), UserDto.class);
        }
        throw new IOException("Request failed: " + response.statusCode() + " - " + response.body());
    }

    public UserDto updateUser(UUID id, UserDto user) throws IOException, InterruptedException {
        return put("/api/users/" + id, user, UserDto.class);
    }

    public void deleteUser(UUID id) throws IOException, InterruptedException {
        delete("/api/users/" + id);
    }

    public List<CategoryDto> getAllCategories() throws IOException, InterruptedException {
        return getList("/api/categories", new TypeReference<List<CategoryDto>>() {});
    }

    public CategoryDto getCategoryById(UUID id) throws IOException, InterruptedException {
        return get("/api/categories/" + id, CategoryDto.class);
    }

    public CategoryDto createCategory(CategoryDto category) throws IOException, InterruptedException {
        return post("/api/categories", category, CategoryDto.class);
    }

    public CategoryDto updateCategory(UUID id, CategoryDto category) throws IOException, InterruptedException {
        return put("/api/categories/" + id, category, CategoryDto.class);
    }

    public void deleteCategory(UUID id) throws IOException, InterruptedException {
        delete("/api/categories/" + id);
    }

    public List<SubscriptionDto> getAllSubscriptions() throws IOException, InterruptedException {
        return getList("/api/subscriptions", new TypeReference<List<SubscriptionDto>>() {});
    }

    public SubscriptionDto getSubscriptionById(UUID id) throws IOException, InterruptedException {
        return get("/api/subscriptions/" + id, SubscriptionDto.class);
    }

    public SubscriptionDto createSubscription(SubscriptionDto subscription) throws IOException, InterruptedException {
        return post("/api/subscriptions", subscription, SubscriptionDto.class);
    }

    public SubscriptionDto updateSubscription(UUID id, SubscriptionDto subscription) throws IOException, InterruptedException {
        return put("/api/subscriptions/" + id, subscription, SubscriptionDto.class);
    }

    public SubscriptionDto updateSubscriptionWithPayment(UUID id, UpdateSubscriptionRequest request) throws IOException, InterruptedException {
        return put("/api/subscriptions/" + id + "/with-payment", request, SubscriptionDto.class);
    }

    public void deleteSubscription(UUID id) throws IOException, InterruptedException {
        delete("/api/subscriptions/" + id);
    }

    public List<PaymentDto> getAllPayments() throws IOException, InterruptedException {
        return getList("/api/payments", new TypeReference<List<PaymentDto>>() {});
    }

    public PaymentDto getPaymentById(UUID id) throws IOException, InterruptedException {
        return get("/api/payments/" + id, PaymentDto.class);
    }

    public PaymentDto updatePayment(UUID id, PaymentDto payment) throws IOException, InterruptedException {
        return put("/api/payments/" + id, payment, PaymentDto.class);
    }

    public List<NotificationDto> getAllNotifications() throws IOException, InterruptedException {
        return getList("/api/notifications", new TypeReference<List<NotificationDto>>() {});
    }

    public List<NotificationDto> getMyReceivedNotifications() throws IOException, InterruptedException {
        return getList("/api/notifications/my/received", new TypeReference<List<NotificationDto>>() {});
    }

    public List<NotificationDto> getSentNotifications() throws IOException, InterruptedException {
        return getList("/api/notifications/sent/true", new TypeReference<List<NotificationDto>>() {});
    }

    public NotificationDto getNotificationById(UUID id) throws IOException, InterruptedException {
        return get("/api/notifications/" + id, NotificationDto.class);
    }

    public void deleteNotification(UUID id) throws IOException, InterruptedException {
        delete("/api/notifications/" + id);
    }

    public Integer markPendingNotificationsAsSent() throws IOException, InterruptedException {
        HttpRequest request = createRequestBuilder("/api/notifications/mark-pending-sent")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Integer.class);
        }
        throw new IOException("Request failed: " + response.statusCode());
    }

    public UserStatisticsDto getMyStatistics() throws IOException, InterruptedException {
        return get("/api/analytics/my-statistics", UserStatisticsDto.class);
    }

    public List<UpcomingPaymentDto> getMyUpcomingPayments() throws IOException, InterruptedException {
        return getList("/api/analytics/my-upcoming-payments", new TypeReference<List<UpcomingPaymentDto>>() {});
    }

    public List<CategoryStatisticsDto> getMyCategoryStatistics() throws IOException, InterruptedException {
        return getList("/api/analytics/my-category-statistics", new TypeReference<List<CategoryStatisticsDto>>() {});
    }

    public UserStatisticsDto getUserStatistics(UUID userId) throws IOException, InterruptedException {
        if (isAdmin()) {
            return get("/api/analytics/users/" + userId + "/statistics", UserStatisticsDto.class);
        } else {
            return getMyStatistics();
        }
    }

    public List<UpcomingPaymentDto> getUpcomingPayments(UUID userId) throws IOException, InterruptedException {
        if (isAdmin()) {
            return getList("/api/analytics/users/" + userId + "/upcoming-payments", new TypeReference<List<UpcomingPaymentDto>>() {});
        } else {
            return getMyUpcomingPayments();
        }
    }

    public List<CategoryStatisticsDto> getCategoryStatistics(UUID userId) throws IOException, InterruptedException {
        if (isAdmin()) {
            return getList("/api/analytics/users/" + userId + "/category-statistics", new TypeReference<List<CategoryStatisticsDto>>() {});
        } else {
            return getMyCategoryStatistics();
        }
    }

    public UserDto updateUserEmail(UUID userId, String email) throws IOException, InterruptedException {
        java.util.Map<String, String> requestBody = new java.util.HashMap<>();
        requestBody.put("email", email);
        return put("/api/auth/" + userId + "/email", requestBody, UserDto.class);
    }

    public void changePassword(UUID userId, String currentPassword, String newPassword) throws IOException, InterruptedException {
        java.util.Map<String, String> requestBody = new java.util.HashMap<>();
        requestBody.put("currentPassword", currentPassword);
        requestBody.put("newPassword", newPassword);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = createRequestBuilder("/api/auth/" + userId + "/change-password")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Request failed: " + response.statusCode() + " - " + response.body());
        }
    }
}