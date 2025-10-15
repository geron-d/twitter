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
 * This configuration class sets up the OpenAPI documentation for the Users API service,
 * including API information, contact details, licensing, and server configurations.
 * It provides a comprehensive documentation setup that follows OpenAPI 3.0 standards.
 *
 * @author geron
 * @version 1.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures the OpenAPI specification for the Users API.
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
    public OpenAPI usersApiOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Twitter Users API")
                .description("""
                    REST API for user management in the Twitter microservices system.
                    
                    This API provides comprehensive user management capabilities including:
                    - User registration and authentication
                    - User profile management
                    - Role-based access control (USER, ADMIN, MODERATOR)
                    - User status management (ACTIVE, INACTIVE)
                    - Advanced filtering and pagination
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
                    .url("http://localhost:8081")
                    .description("Local development server")
            ));
    }
}

