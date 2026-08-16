package com.subscriptionmonitor.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscriptionmonitor.model.entity.User;
import com.subscriptionmonitor.model.enums.CategoryType;
import com.subscriptionmonitor.repository.CategoryRepository;
import com.subscriptionmonitor.repository.PaymentRepository;
import com.subscriptionmonitor.repository.SubscriptionRepository;
import com.subscriptionmonitor.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Subscription Flow Integration Tests")
class SubscriptionFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private User createUser(String username) {
        User user = new User(username, username + "@example.com", "encoded-password");
        return userRepository.save(user);
    }

    private UUID systemCategoryId() {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getType() == CategoryType.SYSTEM)
                .findFirst()
                .orElseThrow()
                .getId();
    }

    private MockHttpServletRequestBuilder asUser(MockHttpServletRequestBuilder builder, String username) {
        return builder.with(user(username).roles("USER")).with(csrf());
    }

    private UUID createPayment(String username) throws Exception {
        String body = """
                {"cost":499.00,"currency":"RUB","billingPeriodDays":30,"nextBillingDate":"%s"}
                """.formatted(LocalDate.now().plusDays(20));
        String response = mockMvc.perform(asUser(post("/api/payments"), username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private UUID createSubscription(String username, String name) throws Exception {
        UUID paymentId = createPayment(username);
        String body = """
                {"name":"%s","categoryId":"%s","paymentId":"%s","isActive":true}
                """.formatted(name, systemCategoryId(), paymentId);
        String response = mockMvc.perform(asUser(post("/api/subscriptions"), username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    @Test
    @DisplayName("Создание подписки с платежом через API")
    void testCreateSubscription_Success() throws Exception {
        createUser("sub_create_user");
        UUID id = createSubscription("sub_create_user", "Netflix");

        mockMvc.perform(asUser(get("/api/subscriptions/" + id), "sub_create_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Netflix"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("Пользователь видит только свои подписки")
    void testGetSubscriptions_OwnershipIsolation() throws Exception {
        createUser("sub_owner_a");
        createUser("sub_owner_b");
        createSubscription("sub_owner_a", "Spotify");

        String responseA = mockMvc.perform(asUser(get("/api/subscriptions"), "sub_owner_a"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String responseB = mockMvc.perform(asUser(get("/api/subscriptions"), "sub_owner_b"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode listA = objectMapper.readTree(responseA);
        JsonNode listB = objectMapper.readTree(responseB);
        assertTrue(listA.size() >= 1);
        assertTrue(listB.isEmpty());
    }

    @Test
    @DisplayName("Чужая подписка недоступна по прямой ссылке")
    void testGetSubscription_ForbiddenForStranger() throws Exception {
        createUser("sub_secret_owner");
        createUser("sub_stranger");
        UUID id = createSubscription("sub_secret_owner", "Secret Service");

        mockMvc.perform(asUser(get("/api/subscriptions/" + id), "sub_stranger"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Удаление подписки каскадно удаляет платёж")
    void testDeleteSubscription_CascadesToPayment() throws Exception {
        createUser("sub_cascade_user");
        UUID id = createSubscription("sub_cascade_user", "Cascade Test");
        UUID paymentId = subscriptionRepository.findById(id).orElseThrow().getPayment().getId();

        mockMvc.perform(asUser(delete("/api/subscriptions/" + id), "sub_cascade_user"))
                .andExpect(status().isNoContent());

        assertFalse(subscriptionRepository.existsById(id));
        assertFalse(paymentRepository.existsById(paymentId));
    }

    @Test
    @DisplayName("Удаление пользователя каскадно удаляет его подписки на уровне БД")
    void testDeleteUser_CascadesToSubscriptions() throws Exception {
        User user = createUser("sub_user_cascade");
        createSubscription("sub_user_cascade", "Doomed Subscription");

        userRepository.deleteById(user.getId());

        assertTrue(subscriptionRepository.findByUserId(user.getId()).isEmpty());
    }

    @Test
    @DisplayName("Две активные подписки с одним именем запрещены")
    void testCreateSubscription_DuplicateActiveName() throws Exception {
        createUser("sub_dup_user");
        createSubscription("sub_dup_user", "Duplicate Name");

        UUID paymentId = createPayment("sub_dup_user");
        String body = """
                {"name":"Duplicate Name","categoryId":"%s","paymentId":"%s","isActive":true}
                """.formatted(systemCategoryId(), paymentId);

        mockMvc.perform(asUser(post("/api/subscriptions"), "sub_dup_user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());
    }
}
