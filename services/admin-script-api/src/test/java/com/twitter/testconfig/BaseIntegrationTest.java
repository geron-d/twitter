package com.twitter.testconfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

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
        registry.add("app.tweet-api.base-url", () -> "http://localhost:" + wireMockPort);
        registry.add("app.follower-api.base-url", () -> "http://localhost:" + wireMockPort);
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
            WireMock.configureFor("localhost", wireMockServer.port());
        }
    }

    /**
     * Gets the WireMock server instance for use in test subclasses.
     *
     * @return WireMockServer instance
     */
    protected WireMockServer getWireMockServer() {
        synchronized (BaseIntegrationTest.class) {
            return wireMockServer;
        }
    }
}
