# Changelog - Реализация эндпоинта GET /api/v1/tweets/{tweetId}/likes

## 2025-01-27

### Step #2: Обновление LikeRepository
- **Время:** 2025-01-27
- **Автор:** assistant
- **Описание:** Добавлен метод findByTweetIdOrderByCreatedAtDesc в LikeRepository для получения лайков с пагинацией
- **Изменения:**
  - Добавлен метод `Page<Like> findByTweetIdOrderByCreatedAtDesc(UUID tweetId, Pageable pageable)` в LikeRepository
  - Добавлены импорты `org.springframework.data.domain.Page` и `org.springframework.data.domain.Pageable`
  - Метод следует паттерну Derived Query Methods из Spring Data JPA
  - JavaDoc не требуется согласно STANDART_JAVADOC.md (Derived Query Methods являются self-documenting)
- **Файлы:**
  - `services/tweet-api/src/main/java/com/twitter/repository/LikeRepository.java`

### Step #3: Обновление LikeMapper
- **Время:** 2025-01-27
- **Автор:** assistant
- **Описание:** Добавлен метод toLikeResponseDtoPage в LikeMapper для маппинга Page<Like> в Page<LikeResponseDto>
- **Изменения:**
  - Добавлен default метод `Page<LikeResponseDto> toLikeResponseDtoPage(Page<Like> likes)` в LikeMapper
  - Метод использует существующий `toLikeResponseDto` для маппинга каждого элемента Page
  - Сохраняет метаданные пагинации (totalElements, totalPages, number, size)
  - Добавлен импорт `org.springframework.data.domain.Page`
  - Добавлен JavaDoc для метода согласно STANDART_JAVADOC.md
- **Файлы:**
  - `services/tweet-api/src/main/java/com/twitter/mapper/LikeMapper.java`

### Step #4: Service метод для эндпоинта
- **Время:** 2025-01-27
- **Автор:** assistant
- **Описание:** Добавлен метод getLikesByTweetId в LikeService интерфейс и реализация в LikeServiceImpl
- **Изменения:**
  - Добавлен метод `Page<LikeResponseDto> getLikesByTweetId(UUID tweetId, Pageable pageable)` в LikeService интерфейс
  - Добавлен полный JavaDoc для метода в интерфейсе согласно STANDART_JAVADOC.md
  - Реализован метод в LikeServiceImpl с валидацией существования твита через LikeValidator
  - Использует `@Transactional(readOnly = true)` для read-only операции
  - Валидация существования твита вынесена в LikeValidator.validateTweetExists
  - Использует `likeRepository.findByTweetIdOrderByCreatedAtDesc` для получения лайков
  - Использует `likeMapper.toLikeResponseDtoPage` для маппинга
  - Добавлены импорты `Page`, `Pageable`
  - Добавлено логирование для отладки
  - Добавлен метод `validateTweetExists` в LikeValidator интерфейс и реализация в LikeValidatorImpl
  - Удален неиспользуемый импорт `BusinessRuleValidationException` из LikeServiceImpl
- **Файлы:**
  - `services/tweet-api/src/main/java/com/twitter/service/LikeService.java`
  - `services/tweet-api/src/main/java/com/twitter/service/LikeServiceImpl.java`
  - `services/tweet-api/src/main/java/com/twitter/validation/LikeValidator.java`
  - `services/tweet-api/src/main/java/com/twitter/validation/LikeValidatorImpl.java`