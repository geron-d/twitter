package com.twitter.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.common.dto.request.user.UserRequestDto;
import com.twitter.common.enums.UserRole;
import com.twitter.common.enums.UserStatus;
import com.twitter.common.exception.validation.BusinessRuleValidationException;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.common.exception.validation.UniquenessValidationException;
import com.twitter.dto.UserPatchDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.entity.User;
import com.twitter.repository.UserRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserValidatorImplTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserValidatorImpl userValidator;

    private UUID testUserId;
    private UserRequestDto testUserRequestDto;
    private UserUpdateDto testUserUpdateDto;
    private UserPatchDto testUserPatchDto;

    @BeforeEach
    void setUp() {
        testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        testUserRequestDto = new UserRequestDto(
            "testuser",
            "Test",
            "User",
            "test@example.com",
            "password123"
        );

        testUserUpdateDto = new UserUpdateDto(
            "updateduser",
            "Updated",
            "User",
            "updated@example.com",
            "newpassword123"
        );

        testUserPatchDto = new UserPatchDto();
        testUserPatchDto.setLogin("patcheduser");
        testUserPatchDto.setEmail("patched@example.com");
    }

    @Nested
    class ValidateForCreateTest {

        @Test
        void validateForCreate_WithValidData_ShouldNotThrowException() {
            when(userRepository.existsByLogin(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);

            assertThatCode(() -> userValidator.validateForCreate(testUserRequestDto)).doesNotThrowAnyException();

            verify(userRepository).existsByLogin("testuser");
            verify(userRepository).existsByEmail("test@example.com");
        }

        @Test
        void validateForCreate_WhenLoginExists_ShouldThrowUniquenessValidationException() {
            when(userRepository.existsByLogin("testuser")).thenReturn(true);

            assertThatThrownBy(() -> userValidator.validateForCreate(testUserRequestDto))
                .isInstanceOf(UniquenessValidationException.class)
                .hasMessageContaining("User with login 'testuser' already exists");

            verify(userRepository).existsByLogin("testuser");
            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        void validateForCreate_WhenEmailExists_ShouldThrowUniquenessValidationException() {
            when(userRepository.existsByLogin("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userValidator.validateForCreate(testUserRequestDto))
                .isInstanceOf(UniquenessValidationException.class)
                .hasMessageContaining("User with email 'test@example.com' already exists");

            verify(userRepository).existsByLogin("testuser");
            verify(userRepository).existsByEmail("test@example.com");
        }
    }

    @Nested
    class ValidateForUpdateTest {

        @Test
        void validateForUpdate_WithValidData_ShouldNotThrowException() {
            when(userRepository.existsByLoginAndIdNot(anyString(), any(UUID.class))).thenReturn(false);
            when(userRepository.existsByEmailAndIdNot(anyString(), any(UUID.class))).thenReturn(false);

            assertThatCode(() -> userValidator.validateForUpdate(testUserId, testUserUpdateDto)).doesNotThrowAnyException();

            verify(userRepository).existsByLoginAndIdNot("updateduser", testUserId);
            verify(userRepository).existsByEmailAndIdNot("updated@example.com", testUserId);
        }

        @Test
        void validateForUpdate_WhenLoginExists_ShouldThrowUniquenessValidationException() {
            when(userRepository.existsByLoginAndIdNot("updateduser", testUserId)).thenReturn(true);

            assertThatThrownBy(() -> userValidator.validateForUpdate(testUserId, testUserUpdateDto))
                .isInstanceOf(UniquenessValidationException.class)
                .hasMessageContaining("User with login 'updateduser' already exists");

            verify(userRepository).existsByLoginAndIdNot("updateduser", testUserId);
            verify(userRepository, never()).existsByEmailAndIdNot(anyString(), any(UUID.class));
        }

        @Test
        void validateForUpdate_WhenEmailExists_ShouldThrowUniquenessValidationException() {
            when(userRepository.existsByLoginAndIdNot("updateduser", testUserId)).thenReturn(false);
            when(userRepository.existsByEmailAndIdNot("updated@example.com", testUserId)).thenReturn(true);

            assertThatThrownBy(() -> userValidator.validateForUpdate(testUserId, testUserUpdateDto))
                .isInstanceOf(UniquenessValidationException.class)
                .hasMessageContaining("User with email 'updated@example.com' already exists");

            verify(userRepository).existsByLoginAndIdNot("updateduser", testUserId);
            verify(userRepository).existsByEmailAndIdNot("updated@example.com", testUserId);
        }
    }

    @Nested
    class ValidateForPatchTest {

        @Test
        void validateForPatch_WithValidJson_ShouldNotThrowException() throws Exception {
            JsonNode validPatch = objectMapper.readTree("{\"login\": \"newlogin\"}");

            assertThatCode(() -> userValidator.validateForPatch(testUserId, validPatch)).doesNotThrowAnyException();
        }

        @Test
        void validateForPatch_WithNullPatch_ShouldThrowFormatValidationException() {
            assertThatThrownBy(() -> userValidator.validateForPatch(testUserId, null))
                .isInstanceOf(FormatValidationException.class)
                .hasMessageContaining("Patch data cannot be null");
        }

        @Test
        void validateForPatch_WithNonObjectJson_ShouldThrowFormatValidationException() throws Exception {
            JsonNode invalidPatch = objectMapper.readTree("\"invalid\"");

            assertThatThrownBy(() -> userValidator.validateForPatch(testUserId, invalidPatch))
                .isInstanceOf(FormatValidationException.class)
                .hasMessageContaining("Patch data must be a JSON object");
        }
    }

    @Nested
    class ValidateForPatchWithDtoTest {

        @Test
        void validateForPatchWithDto_WithValidData_ShouldNotThrowException() {
            when(userRepository.existsByLoginAndIdNot(anyString(), any(UUID.class))).thenReturn(false);
            when(userRepository.existsByEmailAndIdNot(anyString(), any(UUID.class))).thenReturn(false);

            assertThatCode(() -> userValidator.validateForPatchWithDto(testUserId, testUserPatchDto)).doesNotThrowAnyException();

            verify(userRepository).existsByLoginAndIdNot("patcheduser", testUserId);
            verify(userRepository).existsByEmailAndIdNot("patched@example.com", testUserId);
        }

        @Test
        void validateForPatchWithDto_WhenBeanValidationFails_ShouldThrowFormatValidationException() {
            UserPatchDto invalidPatchDto = new UserPatchDto();
            invalidPatchDto.setEmail("invalid-email");

            assertThatThrownBy(() -> userValidator.validateForPatchWithDto(testUserId, invalidPatchDto))
                .isInstanceOf(FormatValidationException.class)
                .hasMessageContaining("Validation failed");

            verify(userRepository, never()).existsByLoginAndIdNot(anyString(), any(UUID.class));
        }

        @Test
        void validateForPatchWithDto_WhenLoginExists_ShouldThrowUniquenessValidationException() {
            when(userRepository.existsByLoginAndIdNot("patcheduser", testUserId)).thenReturn(true);

            assertThatThrownBy(() -> userValidator.validateForPatchWithDto(testUserId, testUserPatchDto))
                .isInstanceOf(UniquenessValidationException.class)
                .hasMessageContaining("User with login 'patcheduser' already exists");

            verify(userRepository).existsByLoginAndIdNot("patcheduser", testUserId);
        }
    }

    @Nested
    class ValidateUniquenessTest {

        @Test
        void validateUniqueness_WithValidData_ShouldNotThrowException() {
            when(userRepository.existsByLogin("testlogin")).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

            assertThatCode(() -> userValidator.validateUniqueness("testlogin", "test@example.com", null))
                .doesNotThrowAnyException();

            verify(userRepository).existsByLogin("testlogin");
            verify(userRepository).existsByEmail("test@example.com");
        }

        @Test
        void validateUniqueness_WhenLoginExists_ShouldThrowUniquenessValidationException() {
            when(userRepository.existsByLogin("testlogin")).thenReturn(true);

            assertThatThrownBy(() -> userValidator.validateUniqueness("testlogin", "test@example.com", null))
                .isInstanceOf(UniquenessValidationException.class)
                .hasMessageContaining("User with login 'testlogin' already exists");

            verify(userRepository).existsByLogin("testlogin");
            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        void validateUniqueness_WhenEmailExists_ShouldThrowUniquenessValidationException() {
            when(userRepository.existsByLogin("testlogin")).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userValidator.validateUniqueness("testlogin", "test@example.com", null))
                .isInstanceOf(UniquenessValidationException.class)
                .hasMessageContaining("User with email 'test@example.com' already exists");

            verify(userRepository).existsByLogin("testlogin");
            verify(userRepository).existsByEmail("test@example.com");
        }
    }

    @Nested
    class ValidateAdminDeactivationTest {

        @Test
        void validateAdminDeactivation_WithNonAdminUser_ShouldNotThrowException() {
            User nonAdminUser = new User().setId(testUserId).setRole(UserRole.USER);
            when(userRepository.findById(testUserId)).thenReturn(java.util.Optional.of(nonAdminUser));

            assertThatCode(() -> userValidator.validateAdminDeactivation(testUserId)).doesNotThrowAnyException();

            verify(userRepository).findById(testUserId);
            verify(userRepository, never()).countByRoleAndStatus(any(), any());
        }

        @Test
        void validateAdminDeactivation_WithAdminUserAndMultipleAdmins_ShouldNotThrowException() {
            User adminUser = new User().setId(testUserId).setRole(UserRole.ADMIN);
            when(userRepository.findById(testUserId)).thenReturn(java.util.Optional.of(adminUser));
            when(userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE)).thenReturn(2L);

            assertThatCode(() -> userValidator.validateAdminDeactivation(testUserId)).doesNotThrowAnyException();

            verify(userRepository).findById(testUserId);
            verify(userRepository).countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
        }

        @Test
        void validateAdminDeactivation_WithLastAdmin_ShouldThrowBusinessRuleValidationException() {
            User adminUser = new User().setId(testUserId).setRole(UserRole.ADMIN);
            when(userRepository.findById(testUserId)).thenReturn(java.util.Optional.of(adminUser));
            when(userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE)).thenReturn(1L);

            assertThatThrownBy(() -> userValidator.validateAdminDeactivation(testUserId))
                .isInstanceOf(BusinessRuleValidationException.class)
                .hasMessageContaining("LAST_ADMIN_DEACTIVATION");

            verify(userRepository).findById(testUserId);
            verify(userRepository).countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
        }
    }

    @Nested
    class ValidateRoleChangeTest {

        @Test
        void validateRoleChange_WithNonAdminUser_ShouldNotThrowException() {
            User nonAdminUser = new User().setId(testUserId).setRole(UserRole.USER);
            when(userRepository.findById(testUserId)).thenReturn(java.util.Optional.of(nonAdminUser));

            assertThatCode(() -> userValidator.validateRoleChange(testUserId, UserRole.ADMIN)).doesNotThrowAnyException();

            verify(userRepository).findById(testUserId);
            verify(userRepository, never()).countByRoleAndStatus(any(), any());
        }

        @Test
        void validateRoleChange_WithAdminToUserAndMultipleAdmins_ShouldNotThrowException() {
            User adminUser = new User().setId(testUserId).setRole(UserRole.ADMIN);
            when(userRepository.findById(testUserId)).thenReturn(java.util.Optional.of(adminUser));
            when(userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE)).thenReturn(2L);

            assertThatCode(() -> userValidator.validateRoleChange(testUserId, UserRole.USER)).doesNotThrowAnyException();

            verify(userRepository).findById(testUserId);
            verify(userRepository).countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
        }

        @Test
        void validateRoleChange_WithLastAdminToUser_ShouldThrowBusinessRuleValidationException() {
            User adminUser = new User().setId(testUserId).setRole(UserRole.ADMIN);
            when(userRepository.findById(testUserId)).thenReturn(java.util.Optional.of(adminUser));
            when(userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE)).thenReturn(1L);

            assertThatThrownBy(() -> userValidator.validateRoleChange(testUserId, UserRole.USER))
                .isInstanceOf(BusinessRuleValidationException.class)
                .hasMessageContaining("LAST_ADMIN_ROLE_CHANGE");

            verify(userRepository).findById(testUserId);
            verify(userRepository).countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
        }
    }

    @Nested
    class ValidatePatchDataTest {

        @Test
        void validatePatchData_WithValidObject_ShouldNotThrowException() throws Exception {
            JsonNode validPatch = objectMapper.readTree("{\"login\": \"newlogin\"}");

            assertThatCode(() -> userValidator.validatePatchData(validPatch)).doesNotThrowAnyException();
        }

        @Test
        void validatePatchData_WithNullPatch_ShouldThrowFormatValidationException() {
            assertThatThrownBy(() -> userValidator.validatePatchData(null))
                .isInstanceOf(FormatValidationException.class)
                .hasMessageContaining("Patch data cannot be null");
        }

        @Test
        void validatePatchData_WithNonObjectJson_ShouldThrowFormatValidationException() throws Exception {
            JsonNode invalidPatch = objectMapper.readTree("\"invalid\"");

            assertThatThrownBy(() -> userValidator.validatePatchData(invalidPatch))
                .isInstanceOf(FormatValidationException.class)
                .hasMessageContaining("Patch data must be a JSON object");
        }

        @Test
        void validatePatchData_WithArrayJson_ShouldThrowFormatValidationException() throws Exception {
            JsonNode invalidPatch = objectMapper.readTree("[\"invalid\"]");

            assertThatThrownBy(() -> userValidator.validatePatchData(invalidPatch))
                .isInstanceOf(FormatValidationException.class)
                .hasMessageContaining("Patch data must be a JSON object");
        }
    }

    @Nested
    class ValidatePatchConstraintsTest {

        @Test
        void validatePatchConstraints_WithValidDto_ShouldNotThrowException() {
            assertThatCode(() -> userValidator.validatePatchConstraints(testUserPatchDto)).doesNotThrowAnyException();
        }

        @Test
        void validatePatchConstraints_WithViolations_ShouldThrowFormatValidationException() {
            UserPatchDto invalidPatchDto = new UserPatchDto();
            invalidPatchDto.setEmail("invalid-email");

            assertThatThrownBy(() -> userValidator.validatePatchConstraints(invalidPatchDto))
                .isInstanceOf(FormatValidationException.class)
                .hasMessageContaining("Validation failed");
        }

        @Test
        void validatePatchConstraints_WithMultipleViolations_ShouldIncludeAllInMessage() {
            UserPatchDto invalidPatchDto = new UserPatchDto();
            invalidPatchDto.setEmail("invalid-email");
            invalidPatchDto.setLogin("ab");

            assertThatThrownBy(() -> userValidator.validatePatchConstraints(invalidPatchDto))
                .isInstanceOf(FormatValidationException.class)
                .hasMessageContaining("Validation failed");
        }

        @Test
        void validatePatchConstraints_WithLoginTooShort_ShouldThrowFormatValidationException() {
            UserPatchDto invalidPatchDto = new UserPatchDto();
            invalidPatchDto.setLogin("ab"); // 2 символа - меньше минимального значения 3

            assertThatThrownBy(() -> userValidator.validatePatchConstraints(invalidPatchDto))
                .isInstanceOf(FormatValidationException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("login: Login must be between 3 and 50 characters");
        }

        @Test
        void validatePatchConstraints_WithLoginTooLong_ShouldThrowFormatValidationException() {
            UserPatchDto invalidPatchDto = new UserPatchDto();
            String longLogin = "a".repeat(51); // 51 символ - больше максимального значения 50
            invalidPatchDto.setLogin(longLogin);

            assertThatThrownBy(() -> userValidator.validatePatchConstraints(invalidPatchDto))
                .isInstanceOf(FormatValidationException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("login: Login must be between 3 and 50 characters");
        }
    }
}