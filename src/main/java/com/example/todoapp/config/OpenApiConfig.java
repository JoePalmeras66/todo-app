package com.example.todoapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI todoAppOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Todo App API")
                        .description("RESTful API for managing todos with priority levels and completion tracking")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Todo App Team")
                                .email("support@todoapp.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
