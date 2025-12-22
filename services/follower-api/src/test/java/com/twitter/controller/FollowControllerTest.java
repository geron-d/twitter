package com.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.dto.request.FollowRequestDto;
import com.twitter.dto.response.FollowResponseDto;
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
}

