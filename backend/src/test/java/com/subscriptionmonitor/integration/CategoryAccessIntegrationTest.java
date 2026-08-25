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

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Category Access Integration Tests")
class CategoryAccessIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private MockHttpServletRequestBuilder asUser(MockHttpServletRequestBuilder builder, String username) {
        return builder.with(user(username).roles("USER")).with(csrf());
    }

    private UUID systemCategoryId() {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getType() == CategoryType.SYSTEM)
                .findFirst()
                .orElseThrow()
                .getId();
    }

    @Test
    @DisplayName("Пользователь читает системную категорию")
    void testUserCanReadSystemCategory() throws Exception {
        userRepository.save(new User("cat_reader", "cat_reader@example.com", "encoded"));

        mockMvc.perform(asUser(get("/api/categories/" + systemCategoryId()), "cat_reader"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SYSTEM"));
    }

    @Test
    @DisplayName("Пользователь не может изменить системную категорию")
    void testUserCannotUpdateSystemCategory() throws Exception {
        userRepository.save(new User("cat_editor", "cat_editor@example.com", "encoded"));

        mockMvc.perform(asUser(put("/api/categories/" + systemCategoryId()), "cat_editor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Перехвачено\",\"type\":\"CUSTOM\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Пользователь не может удалить системную категорию")
    void testUserCannotDeleteSystemCategory() throws Exception {
        userRepository.save(new User("cat_remover", "cat_remover@example.com", "encoded"));

        mockMvc.perform(asUser(delete("/api/categories/" + systemCategoryId()), "cat_remover"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Пользователь не может изменить чужую пользовательскую категорию")
    void testUserCannotUpdateForeignCustomCategory() throws Exception {
        userRepository.save(new User("cat_author", "cat_author@example.com", "encoded"));
        userRepository.save(new User("cat_stranger", "cat_stranger@example.com", "encoded"));

        String created = mockMvc.perform(asUser(post("/api/categories"), "cat_author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Личная категория\",\"type\":\"CUSTOM\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String categoryId = created.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        mockMvc.perform(asUser(put("/api/categories/" + categoryId), "cat_stranger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Перехвачено\",\"type\":\"CUSTOM\"}"))
                .andExpect(status().isForbidden());
    }
}
