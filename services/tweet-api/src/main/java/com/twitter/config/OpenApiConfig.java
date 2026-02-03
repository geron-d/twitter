package com.twitter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 *
 * @author geron
 * @version 1.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures the OpenAPI specification for the Tweet API.
     *
     * @return configured OpenAPI instance
     */
    @Bean
    public OpenAPI tweetApiOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Twitter Tweet API")
                .description("""
                    REST API for tweet management in the Twitter microservices system.
                    
                    This API provides comprehensive tweet management capabilities including:
                    - Tweet creation with content validation
                    - User existence verification via users-api integration
                    - Content length validation (1-280 characters)
                    - Business rule enforcement
                    """)
                .version("1.0.0"))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8082")
                    .description("Local development server")
            ));
    }
}




