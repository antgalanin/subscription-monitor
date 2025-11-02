package com.subscriptionmonitor.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Subscription Monitor API",
        version = "1.0",
        description = "API для управления персональными подписками. "
            + "Система позволяет отслеживать расходы на онлайн-сервисы, "
            + "получать уведомления о предстоящих списаниях и управлять категориями подписок.",
        contact = @Contact(
            name = "Галанин Антон Николаевич",
            email = "ant_galanin@icloud.com"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Local Development Server"
        )
    }
)
@SecurityScheme(
    name = "basicAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "basic",
    description = "HTTP Basic Authentication. Используйте username и password зарегистрированного пользователя."
)
public class SwaggerConfig {

    @Bean
    public OpenApiCustomizer sortTagsAlphabetically() {
        return openApi -> {
            List<Tag> tags = Arrays.asList(
                    new Tag().name("Authentication").description("API для аутентификации и регистрации"),
                    new Tag().name("Users").description("API для управления пользователями системы"),
                    new Tag().name("Categories").description("API для управления категориями подписок"),
                    new Tag().name("Subscriptions").description("API для управления подписками"),
                    new Tag().name("Payments").description("API для управления платежной информацией"),
                    new Tag().name("Notifications").description("API для управления уведомлениями о предстоящих списаниях"),
                    new Tag().name("Analytics").description("API для получения аналитических данных и статистики")
            );
            openApi.setTags(tags);

            if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                var schemas = openApi.getComponents().getSchemas();
                var sortedSchemas = new java.util.LinkedHashMap<String, io.swagger.v3.oas.models.media.Schema>();

                List<String> schemaOrder = Arrays.asList(
                        "UserDto",
                        "CategoryDto",
                        "SubscriptionDto",
                        "PaymentDto",
                        "NotificationDto",
                        "ChangePasswordRequest",
                        "UpdateEmailRequest",
                        "UpdateSubscriptionRequest",
                        "UserStatisticsDto",
                        "CategoryStatisticsDto",
                        "UpcomingPaymentDto",
                        "ErrorResponse"
                );

                for (String schemaName : schemaOrder) {
                    if (schemas.containsKey(schemaName)) {
                        sortedSchemas.put(schemaName, schemas.get(schemaName));
                    }
                }

                schemas.forEach((key, value) -> {
                    if (!sortedSchemas.containsKey(key)) {
                        sortedSchemas.put(key, value);
                    }
                });

                openApi.getComponents().setSchemas(sortedSchemas);
            }
        };
    }
}
