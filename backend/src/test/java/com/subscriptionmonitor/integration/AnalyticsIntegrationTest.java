package com.subscriptionmonitor.integration;

import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.CategoryType;
import com.subscriptionmonitor.repository.CategoryRepository;
import com.subscriptionmonitor.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Analytics Integration Tests")
class AnalyticsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private MockHttpServletRequestBuilder asUser(MockHttpServletRequestBuilder builder, String username) {
        return builder.with(user(username).roles("USER")).with(csrf());
    }

    private MockHttpServletRequestBuilder asAdmin(MockHttpServletRequestBuilder builder, String username) {
        return builder.with(user(username).roles("ADMIN")).with(csrf());
    }

    private void createSubscription(String username, String name, String cost, String currency,
                                    int periodDays, LocalDate nextBilling) throws Exception {
        UUID categoryId = categoryRepository.findAll().stream()
                .filter(c -> c.getType() == CategoryType.SYSTEM)
                .findFirst()
                .orElseThrow()
                .getId();

        String paymentBody = """
                {"cost":%s,"currency":"%s","billingPeriodDays":%d,"nextBillingDate":"%s"}
                """.formatted(cost, currency, periodDays, nextBilling);
        String paymentResponse = mockMvc.perform(asUser(post("/api/payments"), username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String paymentId = objectMapper.readTree(paymentResponse).get("id").asText();

        String subscriptionBody = """
                {"name":"%s","categoryId":"%s","paymentId":"%s","isActive":true}
                """.formatted(name, categoryId, paymentId);
        mockMvc.perform(asUser(post("/api/subscriptions"), username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(subscriptionBody))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Витрина нормализует расходы к месячным тратам по валютам")
    void testMyStatistics_MonthlyNormalization() throws Exception {
        userRepository.save(new User("analytics_user", "analytics_user@example.com", "encoded"));
        createSubscription("analytics_user", "Monthly RUB", "300.00", "RUB", 30, LocalDate.now().plusDays(10));
        createSubscription("analytics_user", "Yearly USD", "120.00", "USD", 365, LocalDate.now().plusDays(40));

        mockMvc.perform(asUser(get("/api/analytics/my-statistics"), "analytics_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("analytics_user"))
                .andExpect(jsonPath("$.activeSubscriptions").value(2))
                .andExpect(jsonPath("$.totalCostRub").value(300.00))
                .andExpect(jsonPath("$.totalCostUsd").value(10.00));
    }

    @Test
    @DisplayName("Витрина предстоящих платежей сортирует по дате")
    void testMyUpcomingPayments_OrderedByDate() throws Exception {
        userRepository.save(new User("upcoming_user", "upcoming_user@example.com", "encoded"));
        createSubscription("upcoming_user", "Later Payment", "100.00", "RUB", 30, LocalDate.now().plusDays(25));
        createSubscription("upcoming_user", "Sooner Payment", "200.00", "RUB", 30, LocalDate.now().plusDays(3));

        mockMvc.perform(asUser(get("/api/analytics/my-upcoming-payments"), "upcoming_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].subscriptionName").value("Sooner Payment"))
                .andExpect(jsonPath("$[1].subscriptionName").value("Later Payment"));
    }

    @Test
    @DisplayName("Персональная статистика категорий видит только свои подписки")
    void testMyCategoryStatistics_PersonalScope() throws Exception {
        userRepository.save(new User("cat_stats_user", "cat_stats_user@example.com", "encoded"));
        createSubscription("cat_stats_user", "Category Stats Sub", "500.00", "RUB", 30, LocalDate.now().plusDays(15));

        mockMvc.perform(asUser(get("/api/analytics/my-category-statistics"), "cat_stats_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].activeSubscriptions").value(1))
                .andExpect(jsonPath("$[0].totalCostRub").value(500.00));
    }

    @Test
    @DisplayName("Администратор читает статистику другого пользователя")
    void testAdminReadsForeignStatistics() throws Exception {
        User owner = userRepository.save(new User("stats_owner", "stats_owner@example.com", "encoded"));
        userRepository.save(new User("stats_admin", "stats_admin@example.com", "encoded"));
        createSubscription("stats_owner", "Admin visible", "500.00", "RUB", 30, LocalDate.now().plusDays(7));

        mockMvc.perform(asAdmin(get("/api/analytics/users/" + owner.getId() + "/statistics"), "stats_admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("stats_owner"))
                .andExpect(jsonPath("$.activeSubscriptions").value(1));
    }

    @Test
    @DisplayName("Администратор видит предстоящие платежи другого пользователя")
    void testAdminReadsForeignUpcomingPayments() throws Exception {
        User owner = userRepository.save(new User("upcoming_owner", "upcoming_owner@example.com", "encoded"));
        userRepository.save(new User("upcoming_admin", "upcoming_admin@example.com", "encoded"));
        createSubscription("upcoming_owner", "Admin visible payment", "700.00", "RUB", 30, LocalDate.now().plusDays(3));

        mockMvc.perform(asAdmin(get("/api/analytics/users/" + owner.getId() + "/upcoming-payments"), "upcoming_admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subscriptionName").value("Admin visible payment"));
    }

    @Test
    @DisplayName("Обычный пользователь не видит чужую статистику")
    void testUserCannotReadForeignStatistics() throws Exception {
        User owner = userRepository.save(new User("stats_victim", "stats_victim@example.com", "encoded"));
        userRepository.save(new User("stats_curious", "stats_curious@example.com", "encoded"));

        mockMvc.perform(asUser(get("/api/analytics/users/" + owner.getId() + "/statistics"), "stats_curious"))
                .andExpect(status().isForbidden());
    }
}
