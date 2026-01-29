package com.example.procurement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Component
@Configuration
public class SwaggerConfig {

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    private SecurityScheme basicScheme() {
        return new SecurityScheme()
                .name("basicAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic");
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addServersItem(new io.swagger.v3.oas.models.servers.Server().url("/"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme())
                        .addSecuritySchemes("basicAuth", basicScheme()))
                .info(new Info().title("My REST API")
                        .description("Some custom description of API."));
    }

}
