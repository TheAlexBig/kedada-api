package com.kedada.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI kedadaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kedada API")
                        .version("v1")
                        .description("API REST para difusion y organizacion de eventos en El Salvador."));
    }
}
