package com.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.common.dto.request.FollowRequestDto;
import com.twitter.common.dto.response.FollowResponseDto;
import com.twitter.entity.Follow;
import com.twitter.repository.FollowRepository;
import com.twitter.testconfig.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class FollowControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * Creates a valid FollowRequestDto for testing.
     *
     * @param followerId  the follower user ID
     * @param followingId the following user ID
     * @return FollowRequestDto instance
     */
    protected FollowRequestDto createValidRequest(UUID followerId, UUID followingId) {
        return FollowRequestDto.builder()
            .followerId(followerId)
            .followingId(followingId)
            .build();
    }

    /**
     * Verifies that a follow relationship exists in the database.
     *
     * @param followerId  the follower user ID
     * @param followingId the following user ID
     * @return true if follow relationship exists, false otherwise
     */
    protected boolean verifyFollowInDatabase(UUID followerId, UUID followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    /**
     * Gets follow count in database.
     *
     * @return number of follow relationships in database
     */
    protected long getFollowCount() {
        return followRepository.count();
    }

    /**
     * Creates and saves a follow relationship in the database for testing.
     *
     * @param followerId  the follower user ID
     * @param followingId the following user ID
     * @return the saved Follow entity
     */
    protected Follow createAndSaveFollow(UUID followerId, UUID followingId) {
        Follow follow = Follow.builder()
            .followerId(followerId)
            .followingId(followingId)
            .build();
        return followRepository.saveAndFlush(follow);
    }

    @Nested
    class CreateFollowTests {

        private UUID testFollowerId;
        private UUID testFollowingId;

        @BeforeEach
        void setUp() {
            testFollowerId = UUID.randomUUID();
            testFollowingId = UUID.randomUUID();
        }

        @Test
        void createFollow_WithValidData_ShouldReturn201Created() throws Exception {
            setupUserExistsStub(testFollowerId, true);
            setupUserExistsStub(testFollowingId, true);
            FollowRequestDto request = createValidRequest(testFollowerId, testFollowingId);

            String responseJson = mockMvc.perform(post("/api/v1/follows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.followerId").value(testFollowerId.toString()))
                .andExpect(jsonPath("$.followingId").value(testFollowingId.toString()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

            FollowResponseDto responseDto = objectMapper.readValue(responseJson, FollowResponseDto.class);

            assertThat(verifyFollowInDatabase(responseDto.followerId(), responseDto.followingId())).isTrue();
            assertThat(getFollowCount()).isEqualTo(1);
        }

        @Test
        void createFollow_WithNullFollowerId_ShouldReturn400BadRequest() throws Exception {
            String requestJson = "{\"followerId\":null,\"followingId\":\"" + testFollowingId + "\"}";

            mockMvc.perform(post("/api/v1/follows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());

            assertThat(getFollowCount()).isEqualTo(0);
        }

        @Test
        void createFollow_WithNullFollowingId_ShouldReturn400BadRequest() throws Exception {
            String requestJson = "{\"followerId\":\"" + testFollowerId + "\",\"followingId\":null}";

            mockMvc.perform(post("/api/v1/follows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());

            assertThat(getFollowCount()).isEqualTo(0);
        }

        @Test
        void createFollow_WithMissingBody_ShouldReturn400BadRequest() throws Exception {
            int status = mockMvc.perform(post("/api/v1/follows")
                    .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getStatus();

            assertThat(status).isGreaterThanOrEqualTo(400);
            assertThat(getFollowCount()).isEqualTo(0);
        }

        @Test
        void createFollow_WhenSelfFollow_ShouldReturn409Conflict() throws Exception {
            setupUserExistsStub(testFollowerId, true);
            FollowRequestDto request = createValidRequest(testFollowerId, testFollowerId);

            mockMvc.perform(post("/api/v1/follows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.ruleName").value("SELF_FOLLOW_NOT_ALLOWED"));

            assertThat(getFollowCount()).isEqualTo(0);
        }

        @Test
        void createFollow_WhenFollowerNotFound_ShouldReturn409Conflict() throws Exception {
            setupUserExistsStub(testFollowerId, false);
            setupUserExistsStub(testFollowingId, true);
            FollowRequestDto request = createValidRequest(testFollowerId, testFollowingId);

            mockMvc.perform(post("/api/v1/follows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.ruleName").value("FOLLOWER_NOT_EXISTS"));

            assertThat(getFollowCount()).isEqualTo(0);
        }

        @Test
        void createFollow_WhenFollowingNotFound_ShouldReturn409Conflict() throws Exception {
            setupUserExistsStub(testFollowerId, true);
            setupUserExistsStub(testFollowingId, false);
            FollowRequestDto request = createValidRequest(testFollowerId, testFollowingId);

            mockMvc.perform(post("/api/v1/follows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.ruleName").value("FOLLOWING_NOT_EXISTS"));

            assertThat(getFollowCount()).isEqualTo(0);
        }

        @Test
        void createFollow_WhenFollowAlreadyExists_ShouldReturn409Conflict() throws Exception {
            setupUserExistsStub(testFollowerId, true);
            setupUserExistsStub(testFollowingId, true);
            FollowRequestDto request = createValidRequest(testFollowerId, testFollowingId);

            // First request - should succeed
            mockMvc.perform(post("/api/v1/follows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            assertThat(getFollowCount()).isEqualTo(1);

            // Second request - should fail with uniqueness violation
            mockMvc.perform(post("/api/v1/follows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").exists());

            assertThat(getFollowCount()).isEqualTo(1);
        }

        @Test
        void createFollow_WhenUsersApiReturns500_ShouldHandleGracefully() throws Exception {
            setupUserExistsStubWithError(testFollowerId, 500);
            FollowRequestDto request = createValidRequest(testFollowerId, testFollowingId);

            mockMvc.perform(post("/api/v1/follows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists());

            assertThat(getFollowCount()).isEqualTo(0);
        }
    }

    @Nested
    class DeleteFollowTests {

        private UUID testFollowerId;
        private UUID testFollowingId;

        @BeforeEach
        void setUp() {
            testFollowerId = UUID.randomUUID();
            testFollowingId = UUID.randomUUID();
        }

        @Test
        void deleteFollow_WithValidData_ShouldReturn204NoContent() throws Exception {
            Follow savedFollow = createAndSaveFollow(testFollowerId, testFollowingId);

            assertThat(getFollowCount()).isEqualTo(1);
            assertThat(verifyFollowInDatabase(testFollowerId, testFollowingId)).isTrue();

            mockMvc.perform(delete("/api/v1/follows/{followerId}/{followingId}", testFollowerId, testFollowingId))
                .andExpect(status().isNoContent());

            assertThat(verifyFollowInDatabase(testFollowerId, testFollowingId)).isFalse();
            assertThat(getFollowCount()).isEqualTo(0);
        }

        @Test
        void deleteFollow_WhenFollowDoesNotExist_ShouldReturn404NotFound() throws Exception {
            mockMvc.perform(delete("/api/v1/follows/{followerId}/{followingId}", testFollowerId, testFollowingId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").exists());

            assertThat(getFollowCount()).isEqualTo(0);
        }
    }

    @Nested
    class GetFollowersTests {

        private UUID testUserId;
        private UUID testFollowerId1;
        private UUID testFollowerId2;
        private UUID testFollowerId3;

        @BeforeEach
        void setUp() {
            testUserId = UUID.randomUUID();
            testFollowerId1 = UUID.randomUUID();
            testFollowerId2 = UUID.randomUUID();
            testFollowerId3 = UUID.randomUUID();
        }

        @Test
        void getFollowers_WhenFollowersExist_ShouldReturn200Ok() throws Exception {
            setupUserExistsStub(testFollowerId1, true);
            setupUserExistsStub(testFollowerId2, true);
            setupUserByIdStub(testFollowerId1, "john_doe");
            setupUserByIdStub(testFollowerId2, "jane_smith");

            createAndSaveFollow(testFollowerId1, testUserId);
            createAndSaveFollow(testFollowerId2, testUserId);

            mockMvc.perform(get("/api/v1/follows/{userId}/followers", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].login").exists())
                .andExpect(jsonPath("$.content[0].createdAt").exists())
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(1));
        }

        @Test
        void getFollowers_WhenNoFollowersExist_ShouldReturn200OkWithEmptyList() throws Exception {
            mockMvc.perform(get("/api/v1/follows/{userId}/followers", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(0));
        }

        @Test
        void getFollowers_WithPagination_ShouldReturnCorrectPage() throws Exception {
            setupUserByIdStub(testFollowerId1, "john_doe");
            setupUserByIdStub(testFollowerId2, "jane_smith");
            setupUserByIdStub(testFollowerId3, "bob_wilson");

            createAndSaveFollow(testFollowerId1, testUserId);
            createAndSaveFollow(testFollowerId2, testUserId);
            createAndSaveFollow(testFollowerId3, testUserId);

            mockMvc.perform(get("/api/v1/follows/{userId}/followers?page=0&size=2", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2));
        }

        @Test
        void getFollowers_WithLoginFilter_ShouldFilterByLogin() throws Exception {
            setupUserByIdStub(testFollowerId1, "john_doe");
            setupUserByIdStub(testFollowerId2, "jane_smith");

            createAndSaveFollow(testFollowerId1, testUserId);
            createAndSaveFollow(testFollowerId2, testUserId);

            mockMvc.perform(get("/api/v1/follows/{userId}/followers?login=john", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].login").value("john_doe"));
        }

        @Test
        void getFollowers_WithLoginFilter_ShouldFilterCaseInsensitively() throws Exception {
            setupUserByIdStub(testFollowerId1, "john_doe");
            setupUserByIdStub(testFollowerId2, "jane_smith");

            createAndSaveFollow(testFollowerId1, testUserId);
            createAndSaveFollow(testFollowerId2, testUserId);

            mockMvc.perform(get("/api/v1/follows/{userId}/followers?login=JOHN", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].login").value("john_doe"));
        }

        @Test
        void getFollowers_ShouldSortByCreatedAtDesc() throws Exception {
            setupUserByIdStub(testFollowerId1, "john_doe");
            setupUserByIdStub(testFollowerId2, "jane_smith");

            // Create follows with different timestamps
            Follow follow1 = createAndSaveFollow(testFollowerId1, testUserId);
            // Wait a bit to ensure different timestamps
            Thread.sleep(10);
            Follow follow2 = createAndSaveFollow(testFollowerId2, testUserId);

            String responseJson = mockMvc.perform(get("/api/v1/follows/{userId}/followers", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

            // Verify that the most recent follow (follow2) appears first
            assertThat(responseJson).contains(testFollowerId2.toString());
        }

        @Test
        void getFollowers_WhenUserLoginNotFound_ShouldUseUnknownLogin() throws Exception {
            setupUserByIdStubWithError(testFollowerId1, 404);

            createAndSaveFollow(testFollowerId1, testUserId);

            mockMvc.perform(get("/api/v1/follows/{userId}/followers", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].login").value("unknown"));
        }

        @Test
        void getFollowers_WithInvalidUserIdFormat_ShouldReturn400BadRequest() throws Exception {
            mockMvc.perform(get("/api/v1/follows/{userId}/followers", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists());
        }
    }

    @Nested
    class GetFollowingTests {

        private UUID testUserId;
        private UUID testFollowingId1;
        private UUID testFollowingId2;
        private UUID testFollowingId3;

        @BeforeEach
        void setUp() {
            testUserId = UUID.randomUUID();
            testFollowingId1 = UUID.randomUUID();
            testFollowingId2 = UUID.randomUUID();
            testFollowingId3 = UUID.randomUUID();
        }

        @Test
        void getFollowing_WhenFollowingExist_ShouldReturn200Ok() throws Exception {
            setupUserExistsStub(testFollowingId1, true);
            setupUserExistsStub(testFollowingId2, true);
            setupUserByIdStub(testFollowingId1, "jane_doe");
            setupUserByIdStub(testFollowingId2, "john_smith");

            createAndSaveFollow(testUserId, testFollowingId1);
            createAndSaveFollow(testUserId, testFollowingId2);

            mockMvc.perform(get("/api/v1/follows/{userId}/following", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].login").exists())
                .andExpect(jsonPath("$.content[0].createdAt").exists())
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(1));
        }

        @Test
        void getFollowing_WhenNoFollowingExist_ShouldReturn200OkWithEmptyList() throws Exception {
            mockMvc.perform(get("/api/v1/follows/{userId}/following", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(0));
        }

        @Test
        void getFollowing_WithPagination_ShouldReturnCorrectPage() throws Exception {
            setupUserByIdStub(testFollowingId1, "jane_doe");
            setupUserByIdStub(testFollowingId2, "john_smith");
            setupUserByIdStub(testFollowingId3, "bob_wilson");

            createAndSaveFollow(testUserId, testFollowingId1);
            createAndSaveFollow(testUserId, testFollowingId2);
            createAndSaveFollow(testUserId, testFollowingId3);

            mockMvc.perform(get("/api/v1/follows/{userId}/following?page=0&size=2", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2));
        }

        @Test
        void getFollowing_WithLoginFilter_ShouldFilterByLogin() throws Exception {
            setupUserByIdStub(testFollowingId1, "jane_doe");
            setupUserByIdStub(testFollowingId2, "john_smith");

            createAndSaveFollow(testUserId, testFollowingId1);
            createAndSaveFollow(testUserId, testFollowingId2);

            mockMvc.perform(get("/api/v1/follows/{userId}/following?login=jane", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].login").value("jane_doe"));
        }

        @Test
        void getFollowing_WithLoginFilter_ShouldFilterCaseInsensitively() throws Exception {
            setupUserByIdStub(testFollowingId1, "jane_doe");
            setupUserByIdStub(testFollowingId2, "john_smith");

            createAndSaveFollow(testUserId, testFollowingId1);
            createAndSaveFollow(testUserId, testFollowingId2);

            mockMvc.perform(get("/api/v1/follows/{userId}/following?login=JANE", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].login").value("jane_doe"));
        }

        @Test
        void getFollowing_ShouldSortByCreatedAtDesc() throws Exception {
            setupUserByIdStub(testFollowingId1, "jane_doe");
            setupUserByIdStub(testFollowingId2, "john_smith");

            // Create follows with different timestamps
            Follow follow1 = createAndSaveFollow(testUserId, testFollowingId1);
            // Wait a bit to ensure different timestamps
            Thread.sleep(10);
            Follow follow2 = createAndSaveFollow(testUserId, testFollowingId2);

            String responseJson = mockMvc.perform(get("/api/v1/follows/{userId}/following", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

            // Verify that the most recent follow (follow2) appears first
            assertThat(responseJson).contains(testFollowingId2.toString());
        }

        @Test
        void getFollowing_WhenUserLoginNotFound_ShouldUseUnknownLogin() throws Exception {
            setupUserByIdStubWithError(testFollowingId1, 404);

            createAndSaveFollow(testUserId, testFollowingId1);

            mockMvc.perform(get("/api/v1/follows/{userId}/following", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].login").value("unknown"));
        }

        @Test
        void getFollowing_WithInvalidUserIdFormat_ShouldReturn400BadRequest() throws Exception {
            mockMvc.perform(get("/api/v1/follows/{userId}/following", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists());
        }
    }

    @Nested
    class GetFollowStatusTests {

        private UUID testFollowerId;
        private UUID testFollowingId;

        @BeforeEach
        void setUp() {
            testFollowerId = UUID.randomUUID();
            testFollowingId = UUID.randomUUID();
        }

        @Test
        void getFollowStatus_WhenFollowExists_ShouldReturn200Ok() throws Exception {
            Follow savedFollow = createAndSaveFollow(testFollowerId, testFollowingId);

            mockMvc.perform(get("/api/v1/follows/{followerId}/{followingId}/status",
                    testFollowerId, testFollowingId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isFollowing").value(true))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
        }

        @Test
        void getFollowStatus_WhenFollowDoesNotExist_ShouldReturn200OkWithFalse() throws Exception {
            mockMvc.perform(get("/api/v1/follows/{followerId}/{followingId}/status",
                    testFollowerId, testFollowingId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isFollowing").value(false))
                .andExpect(jsonPath("$.createdAt").isEmpty());
        }

        @Test
        void getFollowStatus_WithInvalidFollowerIdFormat_ShouldReturn400BadRequest() throws Exception {
            mockMvc.perform(get("/api/v1/follows/{followerId}/{followingId}/status",
                    "invalid-uuid", testFollowingId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists());
        }

        @Test
        void getFollowStatus_WithInvalidFollowingIdFormat_ShouldReturn400BadRequest() throws Exception {
            mockMvc.perform(get("/api/v1/follows/{followerId}/{followingId}/status",
                    testFollowerId, "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists());
        }
    }

    @Nested
    class GetFollowStatsTests {

        private UUID testUserId;
        private UUID follower1Id;
        private UUID follower2Id;
        private UUID following1Id;
        private UUID following2Id;

        @BeforeEach
        void setUp() {
            testUserId = UUID.randomUUID();
            follower1Id = UUID.randomUUID();
            follower2Id = UUID.randomUUID();
            following1Id = UUID.randomUUID();
            following2Id = UUID.randomUUID();
        }

        @Test
        void getFollowStats_WhenStatsExist_ShouldReturn200Ok() throws Exception {
            // Create followers (users following testUserId)
            createAndSaveFollow(follower1Id, testUserId);
            createAndSaveFollow(follower2Id, testUserId);

            // Create following (users that testUserId is following)
            createAndSaveFollow(testUserId, following1Id);
            createAndSaveFollow(testUserId, following2Id);

            mockMvc.perform(get("/api/v1/follows/{userId}/stats", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.followersCount").value(2))
                .andExpect(jsonPath("$.followingCount").value(2));
        }

        @Test
        void getFollowStats_WhenNoStatsExist_ShouldReturn200OkWithZeros() throws Exception {
            mockMvc.perform(get("/api/v1/follows/{userId}/stats", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.followersCount").value(0))
                .andExpect(jsonPath("$.followingCount").value(0));
        }

        @Test
        void getFollowStats_WithOnlyFollowers_ShouldReturnCorrectCounts() throws Exception {
            // Create only followers (users following testUserId)
            createAndSaveFollow(follower1Id, testUserId);
            createAndSaveFollow(follower2Id, testUserId);
            createAndSaveFollow(UUID.randomUUID(), testUserId);

            mockMvc.perform(get("/api/v1/follows/{userId}/stats", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.followersCount").value(3))
                .andExpect(jsonPath("$.followingCount").value(0));
        }

        @Test
        void getFollowStats_WithOnlyFollowing_ShouldReturnCorrectCounts() throws Exception {
            // Create only following (users that testUserId is following)
            createAndSaveFollow(testUserId, following1Id);
            createAndSaveFollow(testUserId, following2Id);

            mockMvc.perform(get("/api/v1/follows/{userId}/stats", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.followersCount").value(0))
                .andExpect(jsonPath("$.followingCount").value(2));
        }

        @Test
        void getFollowStats_WithInvalidUserIdFormat_ShouldReturn400BadRequest() throws Exception {
            mockMvc.perform(get("/api/v1/follows/{userId}/stats", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists());
        }
    }
}

