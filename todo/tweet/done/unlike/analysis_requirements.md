# Анализ требований для эндпоинта DELETE /api/v1/tweets/{tweetId}/like

**Дата:** 2025-01-27  
**Шаг:** #22  
**Статус:** Выполнено

## 1. Входные данные

### 1.1 Path параметры
- **tweetId** (UUID, обязательный)
  - Уникальный идентификатор твита, с которого нужно убрать лайк
  - Формат: UUID
  - Пример: `223e4567-e89b-12d3-a456-426614174001`
  - Валидация: должен быть валидным UUID, твит должен существовать и не быть удаленным

### 1.2 Request Body
- **LikeTweetRequestDto** (обязательный)
  - Переиспользуется существующий DTO из POST операции
  - Структура:
    ```json
    {
      "userId": "123e4567-e89b-12d3-a456-426614174000"
    }
    ```
  - Поля:
    - **userId** (UUID, обязательный) - ID пользователя, который убирает лайк
      - Валидация: должен быть валидным UUID, пользователь должен существовать
      - Проверка: пользователь может убрать только свой лайк (проверка через userId в request body)

## 2. Выходные данные

### 2.1 Успешный ответ
- **HTTP Status Code:** `204 No Content`
- **Response Body:** отсутствует (пустое тело ответа)
- **Headers:** стандартные HTTP заголовки

### 2.2 Ошибочные ответы
Все ошибки возвращаются в формате RFC 7807 Problem Details:

- **400 Bad Request:**
  - Валидация формата (невалидный UUID для tweetId или userId)
  - Ошибки Bean Validation (null значения)
  
- **404 Not Found:**
  - Твит не найден или удален
  - Пользователь не найден
  - Лайк не найден (лайк не существует для данной пары tweetId + userId)

- **409 Conflict:**
  - Бизнес-правила нарушены (например, попытка убрать несуществующий лайк)

## 3. Бизнес-правила

### 3.1 Правила валидации
1. **Существование твита:**
   - Твит с указанным `tweetId` должен существовать в базе данных
   - Твит не должен быть удален (`isDeleted = false`)
   - При нарушении: `BusinessRuleValidationException` с кодом `TWEET_NOT_FOUND`

2. **Существование пользователя:**
   - Пользователь с указанным `userId` должен существовать в системе
   - Проверка выполняется через интеграцию с `users-api` (UserGateway)
   - При нарушении: `BusinessRuleValidationException` с кодом `USER_NOT_EXISTS`

3. **Существование лайка:**
   - Лайк для пары `(tweetId, userId)` должен существовать в базе данных
   - Проверка выполняется через `LikeRepository.existsByTweetIdAndUserId()`
   - При нарушении: `BusinessRuleValidationException` с кодом `LIKE_NOT_FOUND`

4. **Право на удаление:**
   - Пользователь может убрать только свой лайк
   - Проверка выполняется через сравнение `userId` из request body с `userId` в записи лайка
   - Неявная проверка: если лайк существует для данной пары `(tweetId, userId)`, значит пользователь имеет право его удалить

### 3.2 Бизнес-логика
1. **Атомарность операции:**
   - Удаление лайка и обновление счетчика должны выполняться в одной транзакции
   - Использование `@Transactional` на уровне сервиса
   - При ошибке - полный откат транзакции

2. **Обновление счетчика лайков:**
   - При успешном удалении лайка счетчик `likesCount` твита должен быть уменьшен на 1
   - Использование метода `tweet.decrementLikesCount()` (требуется добавить в Entity Tweet)
   - Счетчик не должен стать отрицательным (защита на уровне метода `decrementLikesCount`)

3. **Удаление записи:**
   - Запись лайка удаляется из таблицы `tweet_likes` через `LikeRepository.delete()`
   - Используется метод `findByTweetIdAndUserId()` для поиска записи перед удалением

## 4. Затронутые стандарты

### 4.1 STANDART_CODE.md
- **Java 24 Features:** использование Records для DTO (уже реализовано)
- **Spring Boot 3.5.5 Practices:**
  - Конструкторная инъекция зависимостей с `@RequiredArgsConstructor`
  - Использование `@Transactional` для атомарности операций
  - Использование `@Service` для сервисного слоя
- **Architectural Patterns:**
  - Layered Architecture (Controller → Service → Repository)
  - API Interface Separation (LikeApi интерфейс отдельно от LikeController)
  - Service Interface Pattern (LikeService интерфейс + LikeServiceImpl реализация)
- **Exception Handling:**
  - Использование `BusinessRuleValidationException` для бизнес-правил
  - Использование `GlobalExceptionHandler` для централизованной обработки ошибок
  - Формат ответов: RFC 7807 Problem Details

### 4.2 STANDART_PROJECT.md
- **@LoggableRequest:** использование на методе контроллера для автоматического логирования
- **Validation Exception Hierarchy:** использование `BusinessRuleValidationException`
- **Gateway Pattern:** использование `UserGateway` для проверки существования пользователя

### 4.3 STANDART_TEST.md
- **Unit тесты:**
  - Тестирование `LikeServiceImpl.removeLike()` с моками зависимостей
  - Тестирование `LikeValidatorImpl.validateForUnlike()` с моками
  - Использование `@ExtendWith(MockitoExtension.class)`
  - Использование `@Nested` для группировки тестов
  - Использование AssertJ для assertions
  - Naming pattern: `methodName_WhenCondition_ShouldExpectedResult`
- **Integration тесты:**
  - Тестирование через MockMvc
  - Проверка всех HTTP статус-кодов (204, 400, 404, 409)
  - Использование `@SpringBootTest` и `@Transactional`

### 4.4 STANDART_JAVADOC.md
- **Требования:**
  - Все публичные классы и методы должны иметь JavaDoc
  - Обязательные теги: `@author geron`, `@version 1.0`
  - Для методов: `@param`, `@return`, `@throws`
  - Использование `@see` для реализации интерфейсных методов
- **Структура:**
  - Краткое описание в первой строке
  - Детальное описание с использованием `<p>` тегов
  - Примеры для сложных методов

### 4.5 STANDART_SWAGGER.md
- **OpenAPI аннотации:**
  - `@Operation` с `summary` и `description`
  - `@ApiResponses` со всеми возможными статус-кодами
  - `@ApiResponse` с `@ExampleObject` для каждого сценария
  - `@Parameter` для всех параметров метода
- **DTO документация:**
  - `@Schema` на уровне класса и полей
  - Примеры для всех DTO (уже реализовано для `LikeTweetRequestDto`)
- **Response документация:**
  - Успешный ответ: 204 No Content (без тела)
  - Ошибочные ответы: 400, 404, 409 с примерами Problem Details

## 5. Зависимости и интеграции

### 5.1 Внутренние зависимости
- **LikeRepository:** для поиска и удаления лайков
  - Методы: `findByTweetIdAndUserId()`, `delete()`, `existsByTweetIdAndUserId()`
- **TweetRepository:** для получения твита и обновления счетчика
  - Методы: `findByIdAndIsDeletedFalse()`, `saveAndFlush()`
- **LikeValidator:** для валидации данных
  - Метод: `validateForUnlike()` (уже реализован)
- **UserGateway:** для проверки существования пользователя
  - Метод: `existsUser()` (используется внутри валидатора)

### 5.2 Внешние зависимости
- **users-api:** для проверки существования пользователя
  - Интеграция через `UserGateway` и Feign Client
  - Обработка ошибок через Circuit Breaker (если настроен)

## 6. Технические детали

### 6.1 Требуемые изменения в Entity Tweet
- **Добавить метод `decrementLikesCount()`:**
  ```java
  public void decrementLikesCount() {
      if (this.likesCount == null || this.likesCount <= 0) {
          this.likesCount = 0;
      } else {
          this.likesCount--;
      }
  }
  ```
  - Защита от отрицательных значений
  - Обработка null значений

### 6.2 Требуемые изменения в Service
- **Добавить метод `removeLike()` в интерфейс `LikeService`:**
  - Сигнатура: `void removeLike(UUID tweetId, LikeTweetRequestDto requestDto)`
  - Аннотация: `@Transactional` на уровне реализации
- **Реализация в `LikeServiceImpl`:**
  1. Вызов `likeValidator.validateForUnlike(tweetId, requestDto)`
  2. Поиск лайка через `likeRepository.findByTweetIdAndUserId(tweetId, requestDto.userId())`
  3. Удаление лайка через `likeRepository.delete(like)`
  4. Получение твита через `tweetRepository.findByIdAndIsDeletedFalse(tweetId)`
  5. Вызов `tweet.decrementLikesCount()`
  6. Сохранение твита через `tweetRepository.saveAndFlush(tweet)`

### 6.3 Требуемые изменения в Controller
- **Добавить метод `removeLike()` в интерфейс `LikeApi`:**
  - Сигнатура: `ResponseEntity<Void> removeLike(UUID tweetId, LikeTweetRequestDto requestDto)`
  - OpenAPI аннотации: `@Operation`, `@ApiResponses`, `@Parameter`
- **Реализация в `LikeController`:**
  - Аннотация: `@DeleteMapping("/{tweetId}/like")`
  - Аннотация: `@LoggableRequest`
  - Вызов: `likeService.removeLike(tweetId, requestDto)`
  - Возврат: `ResponseEntity.status(HttpStatus.NO_CONTENT).build()`

## 7. Сценарии использования

### 7.1 Успешный сценарий
1. Клиент отправляет `DELETE /api/v1/tweets/{tweetId}/like` с `LikeTweetRequestDto`
2. Валидатор проверяет существование твита, пользователя и лайка
3. Сервис удаляет лайк из БД
4. Сервис уменьшает счетчик лайков твита
5. Контроллер возвращает `204 No Content`

### 7.2 Ошибочные сценарии
1. **Твит не найден:** `404 Not Found` с Problem Details
2. **Пользователь не найден:** `404 Not Found` с Problem Details
3. **Лайк не найден:** `404 Not Found` с Problem Details
4. **Невалидный UUID:** `400 Bad Request` с Problem Details
5. **Null значения:** `400 Bad Request` с Problem Details

## 8. Выводы

### 8.1 Готовые компоненты
- ✅ DTO (`LikeTweetRequestDto`) - переиспользуется
- ✅ Validator (`validateForUnlike`) - уже реализован
- ✅ Repository методы - все необходимые методы существуют
- ✅ Exception handling - через `GlobalExceptionHandler`

### 8.2 Требуемые изменения
- ⚠️ Добавить метод `decrementLikesCount()` в Entity `Tweet`
- ⚠️ Добавить метод `removeLike()` в интерфейс `LikeService`
- ⚠️ Реализовать метод `removeLike()` в `LikeServiceImpl`
- ⚠️ Добавить метод `removeLike()` в интерфейс `LikeApi` с OpenAPI аннотациями
- ⚠️ Реализовать метод `removeLike()` в `LikeController`
- ⚠️ Добавить JavaDoc для всех новых методов
- ⚠️ Создать unit тесты для Service и Validator
- ⚠️ Создать integration тесты для Controller
- ⚠️ Обновить Swagger документацию
- ⚠️ Обновить README.md
- ⚠️ Обновить Postman коллекцию

### 8.3 Стандарты
Все стандарты проекта должны быть соблюдены:
- ✅ STANDART_CODE.md - архитектура, паттерны, обработка ошибок
- ✅ STANDART_PROJECT.md - использование общих компонентов
- ✅ STANDART_TEST.md - структура и именование тестов
- ✅ STANDART_JAVADOC.md - документация всех публичных методов
- ✅ STANDART_SWAGGER.md - полная OpenAPI документация

