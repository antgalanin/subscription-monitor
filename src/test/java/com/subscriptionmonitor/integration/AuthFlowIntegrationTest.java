package com.subscriptionmonitor.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Auth Flow Integration Tests")
class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String registerJson(String username) {
        return """
                {"username":"%s","email":"%s@example.com","password":"password123"}
                """.formatted(username, username);
    }

    private String loginJson(String username, String password) {
        return """
                {"username":"%s","password":"%s"}
                """.formatted(username, password);
    }

    private void register(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(username)))
                .andExpect(status().isCreated());
    }

    private MockHttpSession login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        return session;
    }

    @Test
    @DisplayName("Регистрация нового пользователя")
    void testRegister_Success() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("auth_reg_user")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("auth_reg_user"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("Вход с корректными данными создаёт сессию")
    void testLogin_Success() throws Exception {
        register("auth_login_user");

        MockHttpSession session = login("auth_login_user", "password123");

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("auth_login_user"));
    }

    @Test
    @DisplayName("Вход с неверным паролем возвращает 401")
    void testLogin_WrongPassword() throws Exception {
        register("auth_wrong_pass");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("auth_wrong_pass", "not-the-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("Запрос профиля без сессии возвращает 401")
    void testMe_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    @DisplayName("Выход из системы аннулирует сессию")
    void testLogout_InvalidatesSession() throws Exception {
        register("auth_logout_user");
        MockHttpSession session = login("auth_logout_user", "password123");

        mockMvc.perform(post("/api/auth/logout").with(csrf()).session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST без CSRF-токена отклоняется")
    void testRegister_WithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("auth_no_csrf")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Повторная регистрация с тем же именем отклоняется")
    void testRegister_DuplicateUsername() throws Exception {
        register("auth_dup_user");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("auth_dup_user")))
                .andExpect(status().isBadRequest());
    }
}
