package com.twitter.testconfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Base abstract class for integration tests that require PostgreSQL and WireMock.
 * Provides shared configuration for test containers and WireMock server.
 * <p>
 * This class ensures that:
 * <ul>
 *   <li>PostgreSQL container is started once and shared across all tests</li>
 *   <li>WireMock server is started once and shared across all tests</li>
 *   <li>Spring properties are configured dynamically</li>
 *   <li>WireMock stubs can be set up in subclasses</li>
 * </ul>
 */
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("twitter_test")
        .withUsername("test")
        .withPassword("test");

    private static WireMockServer wireMockServer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        synchronized (BaseIntegrationTest.class) {
            if (wireMockServer == null) {
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
        if (wireMockServer != null) {
            wireMockServer.resetAll();
        }
    }

    @AfterAll
    static void tearDownWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
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
}

