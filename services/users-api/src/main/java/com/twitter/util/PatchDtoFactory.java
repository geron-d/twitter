package com.twitter.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.common.exception.validation.FormatValidationException;
import com.twitter.dto.UserPatchDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Factory class for creating patch DTOs from JSON data.
 * <p>
 * This utility class provides functionality for applying JSON patch operations
 * to existing DTO objects. It uses Jackson ObjectMapper for safe parsing and
 * field updates, ensuring that only specified fields are modified while
 * preserving the integrity of the original object structure.
 * <p>
 * The factory was extracted from UserValidator to improve separation of concerns
 * and provide reusable patch functionality across the application.
 *
 * @author geron
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PatchDtoFactory {

    /**
     * Jackson ObjectMapper for JSON processing and DTO updates.
     * Injected via constructor to ensure proper Spring dependency management.
     */
    private final ObjectMapper objectMapper;

    /**
     * Creates an updated UserPatchDto by applying JSON patch data to an existing DTO.
     * <p>
     * This method uses Jackson's readerForUpdating functionality to safely apply
     * JSON patch operations to the provided DTO object. Only fields present in the
     * patch data are updated, while other fields remain unchanged. This approach
     * ensures data integrity and prevents unintended field modifications.
     *
     * @param userPatchDto the base DTO object to be updated
     * @param patchNode    JSON data containing the fields to be updated
     * @return the updated UserPatchDto with applied changes
     * @throws FormatValidationException if JSON parsing fails or data format is invalid
     */
    public UserPatchDto createPatchDto(UserPatchDto userPatchDto, JsonNode patchNode) {
        try {
            objectMapper.readerForUpdating(userPatchDto).readValue(patchNode);
            return userPatchDto;
        } catch (IOException e) {
            log.error("Error creating patch DTO from JSON node", e);
            throw FormatValidationException.jsonParsingError(e);
        }
    }
}
