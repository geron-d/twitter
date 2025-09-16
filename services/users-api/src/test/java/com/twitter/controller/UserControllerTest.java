package com.twitter.controller;

import com.twitter.entity.User;
import com.twitter.enums.UserRole;
import com.twitter.enums.UserStatus;
import com.twitter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
public class UserControllerTest {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("twitter_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Nested
    class GetUserByIdIntegrationTests {

        @Test
        void getUserById_WithValidExistingUserId_ShouldReturnUserWith200Ok() throws Exception {
            User user = createTestUser("testuser", "Test", "User", "test@example.com");
            User savedUser = userRepository.save(user);
            UUID userId = savedUser.getId();

            mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        void getUserById_WithNonExistentUserId_ShouldReturn404NotFound() throws Exception {
            UUID nonExistentUserId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/users/{id}", nonExistentUserId))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class FindAllIntegrationTests {

        @Test
        void findAll_WithoutFilters_ShouldReturnAllUsersWithPagination() throws Exception {
            createTestUsers();
            mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(10));
        }

        @Test
        void findAll_WithPaginationFirstPage_ShouldReturnFirst10Users() throws Exception {
            createTestUsers();
            mockMvc.perform(get("/api/v1/users?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(10));
        }

        @Test
        void findAll_WithFirstNameContainsFilter_ShouldReturnMatchingUsers() throws Exception {
            createTestUsers();
            mockMvc.perform(get("/api/v1/users?firstNameContains=John"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.content[1].firstName").value("Johnny"));
        }

        @Test
        void findAll_WithLastNameContainsFilter_ShouldReturnMatchingUsers() throws Exception {
            createTestUsers();
            mockMvc.perform(get("/api/v1/users?lastNameContains=Smith"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].lastName").value("Smith"));
        }

        @Test
        void findAll_WithEmailFilter_ShouldReturnMatchingUser() throws Exception {
            createTestUsers();
            mockMvc.perform(get("/api/v1/users?email=john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.content[0].firstName").value("John"));
        }

        @Test
        void findAll_WithLoginFilter_ShouldReturnMatchingUser() throws Exception {
            createTestUsers();
            mockMvc.perform(get("/api/v1/users?login=johndoe"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].login").value("johndoe"))
                .andExpect(jsonPath("$.content[0].firstName").value("John"));
        }

        @Test
        void findAll_WithRoleFilter_ShouldReturnMatchingUsers() throws Exception {
            createTestUsers();
            mockMvc.perform(get("/api/v1/users?role=ADMIN"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].role").value("ADMIN"))
                .andExpect(jsonPath("$.content[1].role").value("ADMIN"));
        }

        @Test
        void findAll_WithCombinedFilters_ShouldReturnMatchingUsers() throws Exception {
            createTestUsers();
            mockMvc.perform(get("/api/v1/users?firstNameContains=John&role=USER&page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.content[0].role").value("USER"))
                .andExpect(jsonPath("$.content[1].firstName").value("Johnny"))
                .andExpect(jsonPath("$.content[1].role").value("USER"));
        }

        @Test
        void findAll_WithSorting_ShouldReturnSortedUsers() throws Exception {
            createTestUsers();
            mockMvc.perform(get("/api/v1/users?sort=firstName,asc&sort=lastName,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.content[0].firstName").value("Admin"))
                .andExpect(jsonPath("$.content[1].firstName").value("Another"));
        }

        @Test
        void findAll_WithNonExistentFilter_ShouldReturnEmptyResult() throws Exception {
            createTestUsers();
            mockMvc.perform(get("/api/v1/users?firstNameContains=NonExistentName"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.totalPages").value(0))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(0));
        }

        @Test
        void findAll_WithPartialMatchFilters_ShouldReturnMatchingUsers() throws Exception {
            createTestUsers();
            mockMvc.perform(get("/api/v1/users?firstNameContains=Ja&lastNameContains=Sm"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Jane"))
                .andExpect(jsonPath("$.content[0].lastName").value("Smith"));
        }

        @Test
        void findAll_WithInvalidRole_ShouldReturn400BadRequest() throws Exception {
            mockMvc.perform(get("/api/v1/users?role=INVALID_ROLE"))
                .andExpect(status().isBadRequest());
        }

        @Test
        void findAll_ShouldReturnCorrectPaginationMetadata() throws Exception {
            createTestUsers();
            mockMvc.perform(get("/api/v1/users?page=0&size=3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.page.size").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(4))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(10));
        }
    }

    private User createTestUser(String login, String firstName, String lastName, String email) {
        return createTestUser(login, firstName, lastName, email, UserRole.USER, UserStatus.ACTIVE);
    }

    private User createTestUser(String login, String firstName, String lastName, String email, UserRole role, UserStatus status) {
        return new User()
            .setLogin(login)
            .setFirstName(firstName)
            .setLastName(lastName)
            .setEmail(email)
            .setPasswordHash("hashedPassword")
            .setPasswordSalt("salt")
            .setStatus(status)
            .setRole(role);
    }

    private void createTestUsers() {
        List<User> testUsers = List.of(
            createTestUser("johndoe", "John", "Doe", "john.doe@example.com", UserRole.USER, UserStatus.ACTIVE),
            createTestUser("jane.smith", "Jane", "Smith", "jane.smith@example.com", UserRole.USER, UserStatus.ACTIVE),
            createTestUser("admin.user", "Admin", "User", "admin@example.com", UserRole.ADMIN, UserStatus.ACTIVE),
            createTestUser("moderator.user", "Moderator", "User", "moderator@example.com", UserRole.MODERATOR, UserStatus.ACTIVE),
            createTestUser("inactive.user", "Inactive", "User", "inactive@example.com", UserRole.USER, UserStatus.INACTIVE),
            createTestUser("johnny.walker", "Johnny", "Walker", "johnny.walker@example.com", UserRole.USER, UserStatus.ACTIVE),
            createTestUser("smith.johnson", "Smith", "Johnson", "smith.johnson@example.com", UserRole.USER, UserStatus.ACTIVE),
            createTestUser("test.user", "Test", "User", "test.user@example.com", UserRole.USER, UserStatus.ACTIVE),
            createTestUser("another.admin", "Another", "Admin", "another.admin@example.com", UserRole.ADMIN, UserStatus.ACTIVE),
            createTestUser("final.user", "Final", "User", "final.user@example.com", UserRole.USER, UserStatus.ACTIVE)
        );

        userRepository.saveAll(testUsers);
    }
}
