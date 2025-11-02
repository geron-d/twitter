# Тестовые сценарии для метода `validateContent`

**Класс:** `TweetValidatorImpl`  
**Метод:** `validateContent(CreateTweetRequestDto requestDto)`  
**Зависимости:** `Validator validator`, `Logger log` (Slf4j)

---

## Успешные сценарии

- **Успешное выполнение при валидном DTO с нормальным контентом**
  - DTO содержит валидный content (1-280 символов, не пустой, не только пробелы)
  - `validator.validate()` возвращает пустой Set
  - Метод завершается без исключений

---

## Ошибочные сценарии и исключения

### Ошибки Bean Validation

- **Бросается FormatValidationException при нарушении @NotBlank (content = null)**
  - DTO содержит `content = null`
  - `validator.validate()` возвращает Set с одним ConstraintViolation для поля "content" с сообщением о @NotBlank
  - Бросается `FormatValidationException` с fieldName="content", constraintName="CONTENT_VALIDATION"
  - Проверяется, что исключение создано через `FormatValidationException.beanValidationError()`

- **Бросается FormatValidationException при нарушении @NotBlank (content = пустая строка)**
  - DTO содержит `content = ""`
  - `validator.validate()` возвращает Set с ConstraintViolation для поля "content"
  - Бросается `FormatValidationException` с fieldName="content", constraintName="CONTENT_VALIDATION"

- **Бросается FormatValidationException при нарушении @Size (content длиной 0 символов)**
  - DTO содержит `content = ""` (нарушает @Size(min=1))
  - `validator.validate()` возвращает Set с ConstraintViolation для поля "content"
  - Бросается `FormatValidationException` с fieldName="content", constraintName="CONTENT_VALIDATION"

- **Бросается FormatValidationException при нарушении @Size (content длиной более 280 символов)**
  - DTO содержит `content` длиной 281+ символов
  - `validator.validate()` возвращает Set с ConstraintViolation для поля "content" с сообщением о превышении максимальной длины
  - Бросается `FormatValidationException` с fieldName="content", constraintName="CONTENT_VALIDATION"
  - В errorMessage содержится информация о propertyPath и message из ConstraintViolation

- **Бросается FormatValidationException при множественных нарушениях Bean Validation**
  - DTO содержит несколько полей с нарушениями (например, content и userId null)
  - `validator.validate()` возвращает Set с несколькими ConstraintViolation
  - Бросается `FormatValidationException` с объединенным errorMessage из всех нарушений
  - errorMessage содержит все propertyPath: message, разделенные запятыми

### Обработка null DTO

- **Бросается NullPointerException при requestDto = null**
  - Вызов метода с `requestDto = null`
  - При вызове `validator.validate(null)` или `requestDto.getContent()` бросается `NullPointerException`
  - Примечание: поведение может зависеть от реализации Validator, но в реальности такой случай возможен

---

## Дополнительные сценарии

- **Проверка корректности форматирования errorMessage при множественных violations**
  - DTO содержит нарушения для нескольких полей
  - `validator.validate()` возвращает Set с несколькими ConstraintViolation
  - Проверяется, что errorMessage содержит все violations в формате "propertyPath: message, propertyPath: message"
  - Проверяется правильность разделения запятыми и пробелами
