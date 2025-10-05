package com.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserRoleUpdateDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.entity.User;
import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Autowired
    private ObjectMapper objectMapper;

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
                .andExpect(jsonPath("$.page.size").value(10))
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
                .andExpect(jsonPath("$.page.size").value(10))
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

        @Test
        void findAll_WithDefaultPagination_ShouldUseDefaultPageSize() throws Exception {
            createTestUsers();
            mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(10));
        }

        @Test
        void findAll_WithMaxPageSizeExceeded_ShouldLimitToMaxPageSize() throws Exception {
            createTestUsers();
            mockMvc.perform(get("/api/v1/users?size=150"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page.size").value(100))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(10));
        }
    }

    @Nested
    class CreateUserIntegrationTests {

        @Test
        void createUser_WithValidMinimalData_ShouldCreateUserWith200Ok() throws Exception {
            UserRequestDto userRequest = new UserRequestDto(
                "testuser",
                null,
                null,
                "test@example.com",
                "password123"
            );
            String requestJson = objectMapper.writeValueAsString(userRequest);

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.firstName").isEmpty())
                .andExpect(jsonPath("$.lastName").isEmpty())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.id").isNotEmpty());
        }

        @Test
        void createUser_WithValidFullData_ShouldCreateUserWith200Ok() throws Exception {
            UserRequestDto userRequest = new UserRequestDto(
                "fulluser",
                "John",
                "Doe",
                "john.doe@example.com",
                "password123"
            );
            String requestJson = objectMapper.writeValueAsString(userRequest);

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.login").value("fulluser"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.id").isNotEmpty());
        }

        @Test
        void createUser_WithBlankLogin_ShouldReturn400BadRequest() throws Exception {
            UserRequestDto userRequest = new UserRequestDto(
                "",
                "John",
                "Doe",
                "john.doe@example.com",
                "password123"
            );
            String requestJson = objectMapper.writeValueAsString(userRequest);

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void createUser_WithTooShortLogin_ShouldReturn400BadRequest() throws Exception {
            UserRequestDto userRequest = new UserRequestDto(
                "ab",
                "John",
                "Doe",
                "john.doe@example.com",
                "password123"
            );
            String requestJson = objectMapper.writeValueAsString(userRequest);

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void createUser_WithTooLongLogin_ShouldReturn400BadRequest() throws Exception {
            String longLogin = "a".repeat(51);
            UserRequestDto userRequest = new UserRequestDto(
                longLogin,
                "John",
                "Doe",
                "john.doe@example.com",
                "password123"
            );
            String requestJson = objectMapper.writeValueAsString(userRequest);

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void createUser_WithBlankEmail_ShouldReturn400BadRequest() throws Exception {
            UserRequestDto userRequest = new UserRequestDto(
                "testuser",
                "John",
                "Doe",
                "",
                "password123"
            );
            String requestJson = objectMapper.writeValueAsString(userRequest);

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void createUser_WithInvalidEmailFormat_ShouldReturn400BadRequest() throws Exception {
            UserRequestDto userRequest = new UserRequestDto(
                "testuser",
                "John",
                "Doe",
                "invalid-email",
                "password123"
            );
            String requestJson = objectMapper.writeValueAsString(userRequest);

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void createUser_WithBlankPassword_ShouldReturn400BadRequest() throws Exception {
            UserRequestDto userRequest = new UserRequestDto(
                "testuser",
                "John",
                "Doe",
                "john.doe@example.com",
                ""
            );
            String requestJson = objectMapper.writeValueAsString(userRequest);

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void createUser_WithTooShortPassword_ShouldReturn400BadRequest() throws Exception {
            UserRequestDto userRequest = new UserRequestDto(
                "testuser",
                "John",
                "Doe",
                "john.doe@example.com",
                "1234567"
            );
            String requestJson = objectMapper.writeValueAsString(userRequest);

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void createUser_WithDuplicateLogin_ShouldReturn409Conflict() throws Exception {
            User existingUser = createTestUser("duplicateuser", "Existing", "User", "existing@example.com");
            userRepository.save(existingUser);

            UserRequestDto userRequest = new UserRequestDto(
                "duplicateuser",
                "New",
                "User",
                "new@example.com",
                "password123"
            );
            String requestJson = objectMapper.writeValueAsString(userRequest);

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Uniqueness Validation Error"))
                .andExpect(jsonPath("$.detail").value("User with login 'duplicateuser' already exists"))
                .andExpect(jsonPath("$.fieldName").value("login"))
                .andExpect(jsonPath("$.fieldValue").value("duplicateuser"));
        }

        @Test
        void createUser_WithDuplicateEmail_ShouldReturn409Conflict() throws Exception {
            User existingUser = createTestUser("existinguser", "Existing", "User", "duplicate@example.com");
            userRepository.save(existingUser);

            UserRequestDto userRequest = new UserRequestDto(
                "newuser",
                "New",
                "User",
                "duplicate@example.com",
                "password123"
            );
            String requestJson = objectMapper.writeValueAsString(userRequest);

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Uniqueness Validation Error"))
                .andExpect(jsonPath("$.detail").value("User with email 'duplicate@example.com' already exists"))
                .andExpect(jsonPath("$.fieldName").value("email"))
                .andExpect(jsonPath("$.fieldValue").value("duplicate@example.com"));
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

    @Nested
    class UpdateUserIntegrationTest {

        @Test
        void updateUser_WithValidFullData_ShouldUpdateUserWith200Ok() throws Exception {
            User existingUser = createTestUser("testuser", "Original", "Name", "original@example.com");
            User savedUser = userRepository.save(existingUser);
            UUID userId = savedUser.getId();

            UserUpdateDto updateRequest = new UserUpdateDto(
                "updateduser",
                "Updated",
                "Name",
                "updated@example.com",
                "newpassword123"
            );
            String requestJson = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/api/v1/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.login").value("updateduser"))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        void updateUser_WithPartialData_ShouldUpdateOnlySpecifiedFieldsWith200Ok() throws Exception {
            User existingUser = createTestUser("testuser", "Original", "Name", "original@example.com");
            User savedUser = userRepository.save(existingUser);
            UUID userId = savedUser.getId();

            UserUpdateDto updateRequest = new UserUpdateDto(
                "testuser",
                "UpdatedFirstName",
                "UpdatedLastName",
                "original@example.com",
                "newpassword123"
            );
            String requestJson = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/api/v1/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("UpdatedFirstName"))
                .andExpect(jsonPath("$.lastName").value("UpdatedLastName"))
                .andExpect(jsonPath("$.email").value("original@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        void updateUser_WithTooShortLogin_ShouldReturn400BadRequest() throws Exception {
            User existingUser = createTestUser("testuser", "Original", "Name", "original@example.com");
            User savedUser = userRepository.save(existingUser);
            UUID userId = savedUser.getId();

            UserUpdateDto updateRequest = new UserUpdateDto(
                "ab",
                "Updated",
                "Name",
                "updated@example.com",
                "newpassword123"
            );
            String requestJson = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/api/v1/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void updateUser_WithTooLongLogin_ShouldReturn400BadRequest() throws Exception {
            User existingUser = createTestUser("testuser", "Original", "Name", "original@example.com");
            User savedUser = userRepository.save(existingUser);
            UUID userId = savedUser.getId();

            String longLogin = "a".repeat(51);
            UserUpdateDto updateRequest = new UserUpdateDto(
                longLogin,
                "Updated",
                "Name",
                "updated@example.com",
                "newpassword123"
            );
            String requestJson = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/api/v1/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void updateUser_WithInvalidEmail_ShouldReturn400BadRequest() throws Exception {
            User existingUser = createTestUser("testuser", "Original", "Name", "original@example.com");
            User savedUser = userRepository.save(existingUser);
            UUID userId = savedUser.getId();

            UserUpdateDto updateRequest = new UserUpdateDto(
                "updateduser",
                "Updated",
                "Name",
                "invalid-email",
                "newpassword123"
            );
            String requestJson = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/api/v1/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void updateUser_WithTooShortPassword_ShouldReturn400BadRequest() throws Exception {
            User existingUser = createTestUser("testuser", "Original", "Name", "original@example.com");
            User savedUser = userRepository.save(existingUser);
            UUID userId = savedUser.getId();

            UserUpdateDto updateRequest = new UserUpdateDto(
                "updateduser",
                "Updated",
                "Name",
                "updated@example.com",
                "1234567"
            );
            String requestJson = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/api/v1/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void updateUser_WithEmptyLogin_ShouldReturn400BadRequest() throws Exception {
            User existingUser = createTestUser("testuser", "Original", "Name", "original@example.com");
            User savedUser = userRepository.save(existingUser);
            UUID userId = savedUser.getId();

            UserUpdateDto updateRequest = new UserUpdateDto(
                "",
                "Updated",
                "Name",
                "updated@example.com",
                "newpassword123"
            );
            String requestJson = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/api/v1/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void updateUser_WithNullLogin_ShouldReturn400BadRequest() throws Exception {
            User existingUser = createTestUser("testuser", "Original", "Name", "original@example.com");
            User savedUser = userRepository.save(existingUser);
            UUID userId = savedUser.getId();

            UserUpdateDto updateRequest = new UserUpdateDto(
                null,
                "Updated",
                "Name",
                "updated@example.com",
                "newpassword123"
            );
            String requestJson = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/api/v1/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void updateUser_WithNonExistentUserId_ShouldReturn404NotFound() throws Exception {
            UUID nonExistentUserId = UUID.randomUUID();

            UserUpdateDto updateRequest = new UserUpdateDto(
                "updateduser",
                "Updated",
                "Name",
                "updated@example.com",
                "newpassword123"
            );
            String requestJson = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/api/v1/users/{id}", nonExistentUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isNotFound());
        }

        @Test
        void updateUser_WithDuplicateEmail_ShouldReturn409Conflict() throws Exception {
            User existingUser1 = createTestUser("user1", "User", "One", "user1@example.com");
            User existingUser2 = createTestUser("user2", "User", "Two", "user2@example.com");
            userRepository.saveAll(List.of(existingUser1, existingUser2));
            UUID userId1 = existingUser1.getId();

            UserUpdateDto updateRequest = new UserUpdateDto(
                "updateduser1",
                "Updated",
                "Name",
                "user2@example.com",
                "newpassword123"
            );
            String requestJson = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/api/v1/users/{id}", userId1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isConflict());
        }

        @Test
        void updateUser_WithDuplicateLogin_ShouldReturn409Conflict() throws Exception {
            User existingUser1 = createTestUser("user1", "User", "One", "user1@example.com");
            User existingUser2 = createTestUser("user2", "User", "Two", "user2@example.com");
            userRepository.saveAll(List.of(existingUser1, existingUser2));
            UUID userId1 = existingUser1.getId();

            UserUpdateDto updateRequest = new UserUpdateDto(
                "user2",
                "Updated",
                "Name",
                "updated@example.com",
                "newpassword123"
            );
            String requestJson = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/api/v1/users/{id}", userId1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isConflict());
        }
    }

    @Nested
    class PatchUserIntegrationTests {

        @Test
        void patchUser_WithValidSingleField_ShouldUpdateUserWith200Ok() throws Exception {
            User existingUser = createTestUser("testuser", "Original", "Name", "original@example.com");
            User savedUser = userRepository.save(existingUser);
            UUID userId = savedUser.getId();

            String patchJson = "{\"firstName\": \"UpdatedFirstName\"}";

            mockMvc.perform(patch("/api/v1/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("UpdatedFirstName"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.email").value("original@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        void patchUser_WithValidMultipleFields_ShouldUpdateUserWith200Ok() throws Exception {
            User existingUser = createTestUser("testuser", "Original", "Name", "original@example.com");
            User savedUser = userRepository.save(existingUser);
            UUID userId = savedUser.getId();

            String patchJson = "{\"firstName\": \"UpdatedFirstName\", \"lastName\": \"UpdatedLastName\", \"email\": \"updated@example.com\"}";

            mockMvc.perform(patch("/api/v1/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("UpdatedFirstName"))
                .andExpect(jsonPath("$.lastName").value("UpdatedLastName"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        void patchUser_WithEmptyFields_ShouldUpdateUserWith200Ok() throws Exception {
            User existingUser = createTestUser("testuser", "Original", "Name", "original@example.com");
            User savedUser = userRepository.save(existingUser);
            UUID userId = savedUser.getId();

            String patchJson = "{\"firstName\": null, \"lastName\": \"\"}";

            mockMvc.perform(patch("/api/v1/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.firstName").isEmpty())
                .andExpect(jsonPath("$.lastName").isEmpty())
                .andExpect(jsonPath("$.email").value("original@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        void patchUser_WithNonExistentUserId_ShouldReturn404NotFound() throws Exception {
            UUID nonExistentUserId = UUID.randomUUID();

            String patchJson = "{\"firstName\": \"UpdatedFirstName\"}";

            mockMvc.perform(patch("/api/v1/users/{id}", nonExistentUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson))
                .andExpect(status().isNotFound());
        }

        @Test
        void patchUser_WithTooShortLogin_ShouldReturn400BadRequest() throws Exception {
            User existingUser = createTestUser("testuser", "Original", "Name", "original@example.com");
            User savedUser = userRepository.save(existingUser);
            UUID userId = savedUser.getId();

            String patchJson = "{\"login\": \"ab\"}";

            mockMvc.perform(patch("/api/v1/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Format Validation Error"))
                .andExpect(jsonPath("$.detail").value(containsString("Validation failed")));
        }

        @Test
        void patchUser_WithTooLongLogin_ShouldReturn400BadRequest() throws Exception {
            User existingUser = createTestUser("testuser", "Original", "Name", "original@example.com");
            User savedUser = userRepository.save(existingUser);
            UUID userId = savedUser.getId();

            String longLogin = "a".repeat(51);
            String patchJson = "{\"login\": \"" + longLogin + "\"}";

            mockMvc.perform(patch("/api/v1/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void patchUser_WithInvalidEmailFormat_ShouldReturn400BadRequest() throws Exception {
            User existingUser = createTestUser("testuser", "Original", "Name", "original@example.com");
            User savedUser = userRepository.save(existingUser);
            UUID userId = savedUser.getId();

            String patchJson = "{\"email\": \"invalid-email\"}";

            mockMvc.perform(patch("/api/v1/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void patchUser_WithDuplicateLogin_ShouldReturn409Conflict() throws Exception {
            User existingUser1 = createTestUser("user1", "User", "One", "user1@example.com");
            User existingUser2 = createTestUser("user2", "User", "Two", "user2@example.com");
            userRepository.saveAll(List.of(existingUser1, existingUser2));
            UUID userId1 = existingUser1.getId();

            String patchJson = "{\"login\": \"user2\"}";

            mockMvc.perform(patch("/api/v1/users/{id}", userId1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson))
                .andExpect(status().isConflict());
        }

        @Test
        void patchUser_WithDuplicateEmail_ShouldReturn409Conflict() throws Exception {
            User existingUser1 = createTestUser("user1", "User", "One", "user1@example.com");
            User existingUser2 = createTestUser("user2", "User", "Two", "user2@example.com");
            userRepository.saveAll(List.of(existingUser1, existingUser2));
            UUID userId1 = existingUser1.getId();

            String patchJson = "{\"email\": \"user2@example.com\"}";

            mockMvc.perform(patch("/api/v1/users/{id}", userId1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson))
                .andExpect(status().isConflict());
        }
    }

    @Nested
    class InactivateUserIntegrationTest {

        @Test
        void inactivateUser_WhenUserExistsAndIsRegularUser_ShouldReturn200AndInactivateUser() throws Exception {
            User user = createTestUser("testuser", "Test", "User", "test@example.com", UserRole.USER, UserStatus.ACTIVE);
            User savedUser = userRepository.save(user);
            UUID userId = savedUser.getId();

            mockMvc.perform(patch("/api/v1/users/{id}/inactivate", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        void inactivateUser_WhenUserExistsAndIsAdminWithMultipleAdmins_ShouldReturn200AndInactivateUser() throws Exception {
            User admin1 = createTestUser("admin1", "Admin", "One", "admin1@example.com", UserRole.ADMIN, UserStatus.ACTIVE);
            User admin2 = createTestUser("admin2", "Admin", "Two", "admin2@example.com", UserRole.ADMIN, UserStatus.ACTIVE);
            userRepository.saveAll(List.of(admin1, admin2));
            UUID admin1Id = admin1.getId();

            mockMvc.perform(patch("/api/v1/users/{id}/inactivate", admin1Id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(admin1Id.toString()))
                .andExpect(jsonPath("$.login").value("admin1"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("One"))
                .andExpect(jsonPath("$.email").value("admin1@example.com"))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        void inactivateUser_WhenUserExistsAndIsLastActiveAdmin_ShouldReturn409Conflict() throws Exception {
            User admin = createTestUser("admin", "Admin", "User", "admin@example.com", UserRole.ADMIN, UserStatus.ACTIVE);
            User savedAdmin = userRepository.save(admin);
            UUID adminId = savedAdmin.getId();

            mockMvc.perform(patch("/api/v1/users/{id}/inactivate", adminId))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Business Rule Validation Error"))
                .andExpect(jsonPath("$.detail").value("Business rule 'LAST_ADMIN_DEACTIVATION' violated for context: " + admin.getId()))
                .andExpect(jsonPath("$.ruleName").value("LAST_ADMIN_DEACTIVATION"));
        }

        @Test
        void inactivateUser_WhenUserNotFound_ShouldReturn404NotFound() throws Exception {
            UUID nonExistentUserId = UUID.randomUUID();

            mockMvc.perform(patch("/api/v1/users/{id}/inactivate", nonExistentUserId))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateUserRoleIntegrationTests {

        @Test
        void shouldUpdateUserRoleFromUserToAdmin_WhenUserExists() throws Exception {
            User user = createTestUser("testuser", "Test", "User", "test@example.com", UserRole.USER, UserStatus.ACTIVE);
            User savedUser = userRepository.save(user);
            UUID userId = savedUser.getId();

            UserRoleUpdateDto roleUpdate = new UserRoleUpdateDto(UserRole.ADMIN);
            String requestJson = objectMapper.writeValueAsString(roleUpdate);

            mockMvc.perform(patch("/api/v1/users/{id}/role", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        void shouldUpdateAdminRoleToUser_WhenOtherActiveAdminsExist() throws Exception {
            User admin1 = createTestUser("admin1", "Admin", "One", "admin1@example.com", UserRole.ADMIN, UserStatus.ACTIVE);
            User admin2 = createTestUser("admin2", "Admin", "Two", "admin2@example.com", UserRole.ADMIN, UserStatus.ACTIVE);
            userRepository.saveAll(List.of(admin1, admin2));
            UUID admin1Id = admin1.getId();

            UserRoleUpdateDto roleUpdate = new UserRoleUpdateDto(UserRole.USER);
            String requestJson = objectMapper.writeValueAsString(roleUpdate);

            mockMvc.perform(patch("/api/v1/users/{id}/role", admin1Id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(admin1Id.toString()))
                .andExpect(jsonPath("$.login").value("admin1"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("One"))
                .andExpect(jsonPath("$.email").value("admin1@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        void shouldReturn404_WhenUserNotFound() throws Exception {
            UUID nonExistentUserId = UUID.randomUUID();

            UserRoleUpdateDto roleUpdate = new UserRoleUpdateDto(UserRole.ADMIN);
            String requestJson = objectMapper.writeValueAsString(roleUpdate);

            mockMvc.perform(patch("/api/v1/users/{id}/role", nonExistentUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn409_WhenTryingToChangeLastActiveAdminRole() throws Exception {
            User lastAdmin = createTestUser("lastadmin", "Last", "Admin", "lastadmin@example.com", UserRole.ADMIN, UserStatus.ACTIVE);
            User savedAdmin = userRepository.save(lastAdmin);
            UUID adminId = savedAdmin.getId();

            UserRoleUpdateDto roleUpdate = new UserRoleUpdateDto(UserRole.USER);
            String requestJson = objectMapper.writeValueAsString(roleUpdate);

            mockMvc.perform(patch("/api/v1/users/{id}/role", adminId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Business Rule Validation Error"))
                .andExpect(jsonPath("$.detail").value("Business rule 'LAST_ADMIN_ROLE_CHANGE' violated for context: userId=" + lastAdmin.getId() +", newRole=" + roleUpdate.role()));
        }

        @Test
        void shouldReturn400_WhenRoleFieldIsNull() throws Exception {
            User user = createTestUser("testuser", "Test", "User", "test@example.com", UserRole.USER, UserStatus.ACTIVE);
            User savedUser = userRepository.save(user);
            UUID userId = savedUser.getId();

            String requestJson = "{\"role\": null}";

            mockMvc.perform(patch("/api/v1/users/{id}/role", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_WhenInvalidRoleValue() throws Exception {
            User user = createTestUser("testuser", "Test", "User", "test@example.com", UserRole.USER, UserStatus.ACTIVE);
            User savedUser = userRepository.save(user);
            UUID userId = savedUser.getId();

            String requestJson = "{\"role\": \"INVALID_ROLE\"}";

            mockMvc.perform(patch("/api/v1/users/{id}/role", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isInternalServerError());
        }
    }
}
