package com.sesac.studyhub.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String JWT_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Study Hub API")
                .description("스터디 모집/모임 관리 서비스 REST API")
                .version("v1"))
            .components(new Components().addSecuritySchemes(JWT_SCHEME,
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList(JWT_SCHEME));
    }
}