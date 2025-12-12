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
     * Creates and configures the OpenAPI specification for the Admin Script API.
     *
     * @return configured OpenAPI instance
     */
    @Bean
    public OpenAPI adminScriptApiOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Twitter Admin Script API")
                .description("""
                    REST API for executing administrative scripts in the Twitter microservices system.
                    """)
                .version("1.0.0"))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8083")
                    .description("Local development server")
            ));
    }
}

