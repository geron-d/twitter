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
     * Применяет JSON патч к существующему DTO объекту, обновляя только указанные поля.
     * Использует Jackson ObjectMapper для безопасного парсинга и обновления полей.
     *
     * @param userPatchDto базовый DTO объект для обновления
     * @param patchNode JSON данные патча с полями для обновления
     * @return обновленный UserPatchDto с примененными изменениями
     * @throws FormatValidationException при ошибке парсинга JSON или неверном формате данных
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
