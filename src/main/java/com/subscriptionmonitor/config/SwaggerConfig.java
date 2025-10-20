package com.subscriptionmonitor.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

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
}
