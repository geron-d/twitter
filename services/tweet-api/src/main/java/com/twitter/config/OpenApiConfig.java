package com.twitter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 * <p>
 * This configuration class sets up the OpenAPI documentation for the Tweet API service,
 * including API information, contact details, licensing, and server configurations.
 * It provides a comprehensive documentation setup that follows OpenAPI 3.0 standards.
 *
 * @author geron
 * @version 1.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures the OpenAPI specification for the Tweet API.
     * <p>
     * This method defines the main OpenAPI specification including:
     * - API title, description, and version
     * - Contact information for API support
     * - License information
     * - Server configurations for different environments
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
                    
                    ## Authentication
                    Currently, the API does not require authentication for basic operations.
                    Future versions will implement JWT-based authentication.
                    
                    ## Rate Limiting
                    API requests are subject to rate limiting to ensure system stability.
                    Please refer to response headers for current rate limit information.
                    
                    ## Error Handling
                    The API uses standard HTTP status codes and follows RFC 7807 Problem Details
                    for error responses, providing detailed information about validation failures
                    and business rule violations.
                    """)
                .version("1.0.0"))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8082")
                    .description("Local development server")
            ));
    }
}

