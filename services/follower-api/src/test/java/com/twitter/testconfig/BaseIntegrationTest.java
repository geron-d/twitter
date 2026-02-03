package com.twitter.testconfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Base abstract class for integration tests that require PostgreSQL and WireMock.
 * Provides shared configuration for test containers and WireMock server.
 * <p>
 * This class ensures that:
 * - PostgreSQL container is started once and shared across all tests
 * - WireMock server is started once and shared across all tests
 * - Spring properties are configured dynamically
 * - WireMock stubs can be set up in subclasses
 *
 * @author geron
 * @version 1.0
 */
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    protected static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine")
        .withDatabaseName("twitter_test")
        .withUsername("test")
        .withPassword("test");

    private static WireMockServer wireMockServer;

    static {
        // Register shutdown hook to ensure WireMock server is stopped when JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (BaseIntegrationTest.class) {
                if (wireMockServer != null && wireMockServer.isRunning()) {
                    wireMockServer.stop();
                }
            }
        }));
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        synchronized (BaseIntegrationTest.class) {
            if (wireMockServer == null || !wireMockServer.isRunning()) {
                if (wireMockServer != null) {
                    wireMockServer.stop();
                }
                wireMockServer = new WireMockServer(0); // Random port
                wireMockServer.start();
            }
        }

        int wireMockPort = wireMockServer.port();
        registry.add("wiremock.server.port", () -> String.valueOf(wireMockPort));
        registry.add("app.users-api.base-url", () -> "http://localhost:" + wireMockPort);
    }

    @BeforeEach
    void resetWireMock() {
        synchronized (BaseIntegrationTest.class) {
            if (wireMockServer == null || !wireMockServer.isRunning()) {
                if (wireMockServer == null) {
                    wireMockServer = new WireMockServer(0);
                }
                if (!wireMockServer.isRunning()) {
                    wireMockServer.start();
                }
            }
            wireMockServer.resetAll();
        }
    }

    /**
     * Sets up WireMock stub for user existence check.
     *
     * @param userId the user ID to check
     * @param exists whether the user exists
     */
    protected void setupUserExistsStub(UUID userId, boolean exists) {
        if (wireMockServer == null) {
            return;
        }

        wireMockServer.stubFor(
            get(urlEqualTo("/api/v1/users/" + userId + "/exists"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"exists\":" + exists + "}"))
        );
    }

    /**
     * Sets up WireMock stub for user existence check with error response.
     *
     * @param userId     the user ID to check
     * @param statusCode HTTP status code to return
     */
    protected void setupUserExistsStubWithError(UUID userId, int statusCode) {
        if (wireMockServer == null) {
            return;
        }

        wireMockServer.stubFor(
            get(urlEqualTo("/api/v1/users/" + userId + "/exists"))
                .willReturn(aResponse()
                    .withStatus(statusCode)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\":\"Internal Server Error\"}"))
        );
    }

    /**
     * Sets up WireMock stub for getting user by ID.
     *
     * @param userId the user ID
     * @param login  the user login to return
     */
    protected void setupUserByIdStub(UUID userId, String login) {
        if (wireMockServer == null) {
            return;
        }

        String responseBody = String.format("""
            {
              "id": "%s",
              "login": "%s",
              "firstName": "Test",
              "lastName": "User",
              "email": "test@example.com",
              "status": "ACTIVE",
              "role": "USER",
              "createdAt": "2025-01-20T10:00:00"
            }
            """, userId, login);

        wireMockServer.stubFor(
            get(urlEqualTo("/api/v1/users/" + userId))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody))
        );
    }

    /**
     * Sets up WireMock stub for getting user by ID with error response.
     *
     * @param userId     the user ID
     * @param statusCode HTTP status code to return
     */
    protected void setupUserByIdStubWithError(UUID userId, int statusCode) {
        if (wireMockServer == null) {
            return;
        }

        wireMockServer.stubFor(
            get(urlEqualTo("/api/v1/users/" + userId))
                .willReturn(aResponse()
                    .withStatus(statusCode)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\":\"User not found\"}"))
        );
    }
}
