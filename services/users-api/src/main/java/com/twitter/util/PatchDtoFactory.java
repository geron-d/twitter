package com.twitter.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.dto.UserPatchDto;
import com.twitter.exception.validation.FormatValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Утилитный класс для создания DTO из JSON данных патча.
 * Вынесен из UserValidator для лучшего разделения ответственности.
 * 
 * @author Twitter Team
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PatchDtoFactory {
    
    private final ObjectMapper objectMapper;
    
    /**
     * Создает UserPatchDto из JsonNode для валидации.
     *
     * @param patchNode JSON данные патча
     * @return UserPatchDto для валидации
     * @throws FormatValidationException при ошибке парсинга JSON
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
