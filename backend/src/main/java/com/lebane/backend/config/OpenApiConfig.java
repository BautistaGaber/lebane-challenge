package com.lebane.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lebaneOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lebane Real Estate API")
                        .description("API for managing departments, images and inquiries.")
                        .version("v1"));
    }
}
