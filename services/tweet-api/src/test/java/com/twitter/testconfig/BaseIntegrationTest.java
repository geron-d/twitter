package com.twitter.testconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
 */
@Testcontainers
public abstract class BaseIntegrationTest {

    protected static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine")
        .withDatabaseName("twitter_test")
        .withUsername("test")
        .withPassword("test");

    private static WireMockServer wireMockServer;
    private static boolean containersStarted = false;

    static {
        // Запускаем контейнеры только один раз
        synchronized (BaseIntegrationTest.class) {
            if (!containersStarted) {
                postgres.start();
                wireMockServer = new WireMockServer(0);
                wireMockServer.start();
                containersStarted = true;

                // Регистрируем shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (wireMockServer != null && wireMockServer.isRunning()) {
                        wireMockServer.stop();
                    }
                    if (postgres.isRunning()) {
                        postgres.stop();
                    }
                }));
            }
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        int wireMockPort = wireMockServer.port();
        registry.add("wiremock.server.port", () -> String.valueOf(wireMockPort));
        registry.add("app.users-api.base-url", () -> "http://localhost:" + wireMockPort);
        registry.add("app.follower-api.base-url", () -> "http://localhost:" + wireMockPort);
    }

    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
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
     * Sets up WireMock stub for follower-api getFollowing endpoint.
     *
     * @param userId           the user ID whose following list to retrieve
     * @param followingUserIds list of following user IDs to return
     * @param page             page number (default: 0)
     * @param size             page size (default: 100)
     */
    protected void setupFollowingStub(UUID userId, List<UUID> followingUserIds, int page, int size) {
        if (wireMockServer == null) {
            return;
        }

        List<Map<String, String>> content = followingUserIds.stream()
            .map(followingId -> Map.of(
                "id", followingId.toString(),
                "login", "user_" + followingId.toString().substring(0, 8),
                "createdAt", "2025-01-20T15:30:00Z"
            ))
            .toList();

        Map<String, Object> responseBody = Map.of(
            "content", content,
            "page", Map.of(
                "size", size,
                "number", page,
                "totalElements", followingUserIds.size(),
                "totalPages", (followingUserIds.size() + size - 1) / size
            )
        );

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String responseJson = objectMapper.writeValueAsString(responseBody);

            wireMockServer.stubFor(
                get(urlPathEqualTo("/api/v1/follows/" + userId + "/following"))
                    .withQueryParam("page", equalTo(String.valueOf(page)))
                    .withQueryParam("size", equalTo(String.valueOf(size)))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseJson))
            );
        } catch (Exception e) {
            // Ignore JSON serialization errors in tests
        }
    }

    /**
     * Sets up WireMock stub for follower-api getFollowing endpoint with empty result.
     *
     * @param userId the user ID whose following list to retrieve
     */
    protected void setupFollowingStubEmpty(UUID userId) {
        setupFollowingStub(userId, Collections.emptyList(), 0, 100);
    }

    /**
     * Sets up WireMock stub for follower-api getFollowing endpoint with error response.
     *
     * @param userId     the user ID whose following list to retrieve
     * @param statusCode HTTP status code to return
     */
    protected void setupFollowingStubWithError(UUID userId, int statusCode) {
        if (wireMockServer == null) {
            return;
        }

        wireMockServer.stubFor(
            get(urlPathMatching("/api/v1/follows/" + userId + "/following.*"))
                .willReturn(aResponse()
                    .withStatus(statusCode)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\":\"Internal Server Error\"}"))
        );

        wireMockServer.stubFor(
            get(urlPathEqualTo("/api/v1/follows/" + userId + "/following"))
                .willReturn(aResponse()
                    .withStatus(statusCode)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\":\"Internal Server Error\"}"))
        );
    }
}