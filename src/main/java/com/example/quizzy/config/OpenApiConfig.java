package com.example.quizzy.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Quizzy API",
                version = "v1.0",
                description = "API documentation for the Quizzy application."
        ),
        security = @SecurityRequirement(name = "bearerAuth") // Applies security requirements globally
)
@SecurityScheme(
        name = "bearerAuth", // A name for the security scheme
        type = SecuritySchemeType.HTTP, // The type of the scheme
        scheme = "bearer", // The scheme name
        bearerFormat = "JWT" // A hint to the client about the format of the bearer token
)
public class OpenApiConfig {
}
