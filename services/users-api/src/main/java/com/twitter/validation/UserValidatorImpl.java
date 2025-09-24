package com.twitter.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.dto.UserPatchDto;
import com.twitter.dto.UserRequestDto;
import com.twitter.dto.UserUpdateDto;
import com.twitter.entity.User;
import com.twitter.enums.UserRole;
import com.twitter.enums.UserStatus;
import com.twitter.exception.validation.BusinessRuleValidationException;
import com.twitter.exception.validation.FormatValidationException;
import com.twitter.exception.validation.UniquenessValidationException;
import com.twitter.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация валидатора пользователей для системы Twitter.
 * Централизует всю логику валидации, вынесенную из UserServiceImpl.
 *
 * @author Twitter Team
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserValidatorImpl implements UserValidator {

    private final UserRepository userRepository;
    private final Validator validator;

    @Override
    public void validateForCreate(UserRequestDto userRequest) {
        validateUniqueness(userRequest.login(), userRequest.email(), null);
    }

    @Override
    public void validateForUpdate(UUID userId, UserUpdateDto userUpdate) {
        validateUniqueness(userUpdate.login(), userUpdate.email(), userId);
    }

    @Override
    public void validateForPatch(UUID userId, JsonNode patchNode) {
        validatePatchData(patchNode);
    }
    
    @Override
    public void validateForPatchWithDto(UUID userId, UserPatchDto patchDto) {
        validatePatchConstraints(patchDto);
        validateUniqueness(patchDto.getLogin(), patchDto.getEmail(), userId);
    }

    @Override
    public void validateUniqueness(String login, String email, UUID excludeUserId) {
        if (!ObjectUtils.isEmpty(login)) {
            boolean loginExists = excludeUserId != null
                ? userRepository.existsByLoginAndIdNot(login, excludeUserId)
                : userRepository.existsByLogin(login);

            if (loginExists) {
                log.warn("Uniqueness validation failed: login '{}' already exists", login);
                throw new UniquenessValidationException("login", login);
            }
        }

        if (!ObjectUtils.isEmpty(email)) {
            boolean emailExists = excludeUserId != null
                ? userRepository.existsByEmailAndIdNot(email, excludeUserId)
                : userRepository.existsByEmail(email);

            if (emailExists) {
                log.warn("Uniqueness validation failed: email '{}' already exists", email);
                throw new UniquenessValidationException("email", email);
            }
        }
    }

    @Override
    public void validateAdminDeactivation(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessRuleValidationException("USER_NOT_FOUND", userId));

        if (user.getRole() == UserRole.ADMIN) {
            long activeAdminCount = userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
            if (activeAdminCount <= 1) {
                log.warn("Business rule validation failed: attempt to deactivate last admin with ID: {}", userId);
                throw BusinessRuleValidationException.lastAdminDeactivation(userId);
            }
        }
    }

    @Override
    public void validateRoleChange(UUID userId, UserRole newRole) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessRuleValidationException("USER_NOT_FOUND", userId));

        UserRole oldRole = user.getRole();

        if (oldRole == UserRole.ADMIN && newRole != UserRole.ADMIN) {
            long activeAdminCount = userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
            if (activeAdminCount <= 1) {
                log.warn("Business rule validation failed: attempt to change role of last admin with ID: {} from {} to {}",
                    userId, oldRole, newRole);
                throw BusinessRuleValidationException.lastAdminRoleChange(userId, newRole);
            }
        }
    }

    @Override
    public void validatePatchData(JsonNode patchNode) {
        if (patchNode == null || patchNode.isNull()) {
            throw new FormatValidationException("Patch data cannot be null");
        }

        if (!patchNode.isObject()) {
            throw new FormatValidationException("Patch data must be a JSON object");
        }
    }

    @Override
    public void validatePatchConstraints(UserPatchDto patchDto) {
        Set<ConstraintViolation<UserPatchDto>> violations = validator.validate(patchDto);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

            log.warn("Patch constraints validation failed: {}", errorMessage);
            throw new FormatValidationException("Validation failed: " + errorMessage);
        }
    }
}
