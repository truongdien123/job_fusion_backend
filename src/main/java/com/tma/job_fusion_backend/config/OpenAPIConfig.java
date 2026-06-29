package com.tma.job_fusion_backend.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${open.api.serverUrl}")
    private String serverUrl;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("Job Fusion API")
                        .version("1.0")
                        .description("API documentation for Job Fusion Backend"))
                .servers(List.of(new Server().url(serverUrl).description("Local server")))
                .components(
                        new Components().addSecuritySchemes("bearerAuth",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .security(List.of(new SecurityRequirement().addList("bearerAuth")));
    }

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("job-fusion-api")
                .packagesToScan("com.tma.job_fusion_backend.controllers")
                .build();
    }
}
