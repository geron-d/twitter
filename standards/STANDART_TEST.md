# Стандарт написания юнит-тестов

## Обзор

Этот документ определяет стандарты и лучшие практики для написания юнит-тестов в проекте Twitter microservices.

**Технологический стек для тестирования:**
- JUnit 5 (Jupiter)
- Mockito
- AssertJ
- Spring Boot Test (для интеграционных тестов)
- Testcontainers (для интеграционных тестов с БД)
- WireMock (для мокирования внешних сервисов)

---

## 1. Общие принципы

### 1.1 Язык документации

- **Вся документация в тестах должна быть на английском языке**
- Комментарии в тестах должны быть краткими и понятными
- Имена тестов должны быть описательными

### 1.2 Структура тестов

**Тесты должны отражать структуру основного кода:**

```
src/
├── main/
│   └── java/com/twitter/
│       ├── controller/
│       ├── service/
│       ├── mapper/
│       └── validation/
└── test/
    └── java/com/twitter/
        ├── controller/
        ├── service/
        ├── mapper/
        └── validation/
```

### 1.3 Именование тестовых классов

**Формат:** `[ClassName]Test`

Примеры:
- `UserServiceImplTest` - тест для `UserServiceImpl`
- `TweetControllerTest` - тест для `TweetController`
- `UserMapperTest` - тест для `UserMapper`
- `PasswordUtilTest` - тест для `PasswordUtil`

---

## 2. Именование тестовых методов

### 2.1 Стандартный паттерн

**Используйте паттерн:** `methodName_WhenCondition_ShouldExpectedResult`

**Компоненты:**
- `methodName` - имя тестируемого метода
- `WhenCondition` - условие/контекст теста
- `ShouldExpectedResult` - ожидаемый результат

### 2.2 Примеры из существующих тестов

**Успешные сценарии:**
```java
@Test
void createTweet_WithValidData_ShouldReturnTweetResponseDto();

@Test
void getUserById_WhenUserExists_ShouldReturnUser();

@Test
void findAll_WhenUsersExist_ShouldReturnPageWithUsers();

@Test
void createUser_WithValidData_ShouldCreateAndReturnUser();
```

**Сценарии с ошибками:**
```java
@Test
void createTweet_WithEmptyContent_ShouldReturn400BadRequest();

@Test
void getUserById_WhenUserDoesNotExist_ShouldReturnEmptyOptional();

@Test
void createUser_WhenLoginExists_ShouldThrowUniquenessValidationException();

@Test
void validateForCreate_WhenContentIsEmptyString_ShouldThrowFormatValidationException();
```

**Граничные случаи:**
```java
@Test
void createTweet_WithContentExceedingMaxLength_ShouldReturn400BadRequest();

@Test
void createUser_WithTooShortLogin_ShouldReturn400BadRequest();

@Test
void createUser_WithTooLongLogin_ShouldReturn400BadRequest();
```

### 2.3 Альтернативные паттерны

Для простых тестов можно использовать более короткие имена:

```java
@Test
void shouldReturnNonNullSaltArray();

@Test
void shouldReturnSaltArrayWithCorrectLength();

@Test
void shouldReturnDifferentSaltValuesOnMultipleCalls();
```

**Используйте этот паттерн для:**
- Утилитарных классов
- Простых методов без сложной логики
- Тестов, где контекст очевиден

---

## 3. Типы тестов

### 3.1 Unit тесты

**Характеристики:**
- Тестируют один класс/метод изолированно
- Используют моки для всех зависимостей
- Быстрые в выполнении
- Не требуют Spring Context

**Пример:**
```java
@ExtendWith(MockitoExtension.class)
class TweetServiceImplTest {

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private TweetMapper tweetMapper;

    @Mock
    private TweetValidator tweetValidator;

    @InjectMocks
    private TweetServiceImpl tweetService;

    @Test
    void createTweet_WithValidData_ShouldReturnTweetResponseDto() {
        // Test implementation
    }
}
```

### 3.2 Integration тесты

**Характеристики:**
- Тестируют взаимодействие нескольких компонентов
- Используют Spring Boot Test
- Могут использовать реальную БД (Testcontainers)
- Могут использовать WireMock для внешних сервисов

**Пример:**
```java
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class TweetControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void createTweet_WithValidData_ShouldReturn201Created() throws Exception {
        // Test implementation
    }
}
```

### 3.3 Когда использовать каждый тип

**Unit тесты для:**
- Service implementations
- Validators
- Mappers
- Utility classes
- Gateways

**Integration тесты для:**
- Controllers (REST endpoints)
- Repository queries (сложные)
- End-to-end сценарии
- Взаимодействие с внешними сервисами

---

## 4. Структура тестового класса

### 4.1 Базовая структура

```java
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    // 1. Mock dependencies
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserValidator userValidator;

    // 2. Test subject
    @InjectMocks
    private UserServiceImpl userService;

    // 3. Test data (if shared)
    private UUID testUserId;
    private User testUser;
    private UserResponseDto testUserResponseDto;

    // 4. Setup methods
    @BeforeEach
    void setUp() {
        // Initialize test data
    }

    // 5. Nested test classes
    @Nested
    class GetUserByIdTest {
        // Tests for getUserById method
    }

    // 6. Helper methods (if needed)
    private User createTestUser(String login, String email) {
        // Helper implementation
    }
}
```

### 4.2 Использование @Nested для группировки

**Группируйте тесты по функциональности:**

```java
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Nested
    class GetUserByIdTest {
        @Test
        void getUserById_WhenUserExists_ShouldReturnUser() { }

        @Test
        void getUserById_WhenUserDoesNotExist_ShouldReturnEmptyOptional() { }
    }

    @Nested
    class CreateUserTest {
        @Test
        void createUser_WithValidData_ShouldCreateAndReturnUser() { }

        @Test
        void createUser_WhenLoginExists_ShouldThrowUniquenessValidationException() { }
    }

    @Nested
    class UpdateUserTest {
        @Test
        void updateUser_WhenUserExists_ShouldUpdateAndReturnUser() { }

        @Test
        void updateUser_WhenUserDoesNotExist_ShouldReturnEmptyOptional() { }
    }
}
```

**Преимущества:**
- Логическая группировка тестов
- Улучшенная читаемость
- Легче найти тесты для конкретного метода
- Можно использовать общий `@BeforeEach` для группы

### 4.3 @BeforeEach для инициализации

**Используйте для:**
- Инициализации тестовых данных
- Настройки моков (если общая для всех тестов)
- Подготовки окружения

**Пример:**
```java
@Nested
class CreateTweetTests {

    private CreateTweetRequestDto validRequestDto;
    private Tweet mappedTweet;
    private Tweet savedTweet;
    private TweetResponseDto responseDto;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        validRequestDto = CreateTweetRequestDto.builder()
            .content("Hello World")
            .userId(testUserId)
            .build();

        mappedTweet = Tweet.builder()
            .userId(testUserId)
            .content("Hello World")
            .build();

        UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
        savedTweet = Tweet.builder()
            .id(tweetId)
            .userId(testUserId)
            .content("Hello World")
            .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
            .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
            .build();

        responseDto = TweetResponseDto.builder()
            .id(tweetId)
            .userId(testUserId)
            .content("Hello World")
            .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
            .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
            .build();
    }

    @Test
    void createTweet_WithValidData_ShouldReturnTweetResponseDto() {
        // Test uses data from setUp()
    }
}
```

---

## 5. Использование моков

### 5.1 Аннотации Mockito

**@Mock** - для зависимостей:
```java
@Mock
private UserRepository userRepository;

@Mock
private UserMapper userMapper;

@Mock
private UserValidator userValidator;
```

**@InjectMocks** - для тестируемого класса:
```java
@InjectMocks
private UserServiceImpl userService;
```

**@Spy** - для частичных моков (когда нужно реальное поведение с возможностью переопределения):
```java
@Spy
private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
```

### 5.2 Настройка поведения моков

**when().thenReturn()** - для возврата значений:
```java
when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
when(userMapper.toUserResponseDto(testUser)).thenReturn(testUserResponseDto);
```

**doNothing().when()** - для void методов:
```java
doNothing().when(tweetValidator).validateForCreate(validRequestDto);
```

**doThrow().when()** - для исключений:
```java
doThrow(new UniquenessValidationException("login", "testuser"))
    .when(userValidator).validateForCreate(testUserRequestDto);
```

**when().thenAnswer()** - для сложной логики:
```java
when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
    User user = invocation.getArgument(0);
    assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    return savedUser;
});
```

### 5.3 Проверка вызовов моков

**verify()** - проверка вызова метода:
```java
verify(userRepository).findById(testUserId);
verify(userMapper).toUserResponseDto(testUser);
```

**verify() с количеством вызовов:**
```java
verify(tweetValidator, times(1)).validateForCreate(eq(validRequestDto));
verify(userRepository, never()).save(any());
verify(userMapper, never()).toUserResponseDto(any());
```

**verify() с порядком вызовов:**
```java
InOrder inOrder = inOrder(tweetValidator, tweetMapper, tweetRepository);
inOrder.verify(tweetValidator).validateForCreate(validRequestDto);
inOrder.verify(tweetMapper).toEntity(validRequestDto);
inOrder.verify(tweetRepository).saveAndFlush(mappedTweet);
```

### 5.4 ArgumentMatchers

**Используйте ArgumentMatchers для гибкости:**
```java
verify(userRepository).findAll(any(Specification.class), eq(pageable));
verify(userMapper).toUserResponseDto(any(User.class));
verify(userValidator).validateForCreate(any(UserRequestDto.class));
```

**Доступные матчеры:**
- `any()` - любой объект
- `any(Class.class)` - любой объект указанного типа
- `eq(value)` - точное совпадение
- `isNull()` - null значение
- `isNotNull()` - не null значение
- `argThat(condition)` - кастомное условие

---

## 6. Assertions

### 6.1 AssertJ vs JUnit assertions

**Предпочтительно использовать AssertJ** (более читаемый и функциональный):

```java
// AssertJ (предпочтительно)
assertThat(result).isNotNull();
assertThat(result).isEqualTo(expected);
assertThat(result).isPresent();
assertThat(result).isEmpty();

// JUnit (для простых случаев или когда AssertJ избыточен)
assertNotNull(result);
assertEquals(expected, result);
assertTrue(result.isPresent());
```

### 6.2 Базовые assertions

**Проверка на null:**
```java
assertThat(result).isNotNull();
assertThat(result).isNull();
```

**Проверка равенства:**
```java
assertThat(result).isEqualTo(expected);
assertThat(result).isNotEqualTo(other);
```

**Проверка Optional:**
```java
assertThat(result).isPresent();
assertThat(result).isEmpty();
assertThat(result.get()).isEqualTo(expected);
```

**Проверка коллекций:**
```java
assertThat(result).hasSize(2);
assertThat(result).contains(expected);
assertThat(result).containsExactly(item1, item2);
assertThat(result).isEmpty();
```

### 6.3 Проверка исключений

**assertThatThrownBy()** - для проверки исключений:
```java
assertThatThrownBy(() -> userService.createUser(testUserRequestDto))
    .isInstanceOf(UniquenessValidationException.class)
    .hasMessageContaining("User with login 'testuser' already exists");
```

**assertThatCode()** - для проверки отсутствия исключений:
```java
assertThatCode(() -> tweetValidator.validateContent(requestDto))
    .doesNotThrowAnyException();
```

**assertThatThrownBy() с дополнительными проверками:**
```java
assertThatThrownBy(() -> tweetValidator.validateForCreate(requestDto))
    .isInstanceOf(FormatValidationException.class)
    .satisfies(exception -> {
        FormatValidationException ex = (FormatValidationException) exception;
        assertThat(ex.getConstraintName()).isEqualTo("CONTENT_VALIDATION");
        assertThat(ex.getFieldName()).isEqualTo("content");
    });
```

### 6.4 Проверка объектов

**Проверка полей объекта:**
```java
assertThat(result).isNotNull();
assertThat(result.id()).isEqualTo(savedUser.getId());
assertThat(result.login()).isEqualTo("testuser");
assertThat(result.firstName()).isEqualTo("Test");
assertThat(result.email()).isEqualTo("test@example.com");
assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
assertThat(result.role()).isEqualTo(UserRole.USER);
```

**Проверка с сообщениями:**
```java
assertNotNull(result, "TweetResponseDto should not be null");
assertEquals(tweetId, result.id(), "ID should be mapped correctly");
assertEquals(userId, result.userId(), "User ID should be mapped correctly");
```

---

## 7. Тестовые данные

### 7.1 Создание тестовых объектов

**Используйте Builder паттерн (если доступен):**
```java
CreateTweetRequestDto request = CreateTweetRequestDto.builder()
    .content("Hello World")
    .userId(testUserId)
    .build();

Tweet tweet = Tweet.builder()
    .id(tweetId)
    .userId(testUserId)
    .content("Hello World")
    .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
    .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
    .build();
```

**Используйте fluent setters (для entities):**
```java
User user = new User()
    .setId(testUserId)
    .setLogin("testuser")
    .setFirstName("Test")
    .setLastName("User")
    .setEmail("test@example.com")
    .setPasswordHash("hashedPassword")
    .setPasswordSalt("salt")
    .setStatus(UserStatus.ACTIVE)
    .setRole(UserRole.USER);
```

**Используйте конструкторы (для Records):**
```java
UserRequestDto userRequest = new UserRequestDto(
    "testuser",
    "Test",
    "User",
    "test@example.com",
    "password123"
);
```

### 7.2 Helper методы

**Создавайте helper методы для повторяющихся данных:**
```java
private User createTestUser(String login, String firstName, String lastName, String email) {
    return createTestUser(login, firstName, lastName, email, UserRole.USER, UserStatus.ACTIVE);
}

private User createTestUser(String login, String firstName, String lastName, String email, 
                           UserRole role, UserStatus status) {
    return new User()
        .setLogin(login)
        .setFirstName(firstName)
        .setLastName(lastName)
        .setEmail(email)
        .setPasswordHash("hashedPassword")
        .setPasswordSalt("salt")
        .setStatus(status)
        .setRole(role);
}

private void createTestUsers() {
    List<User> testUsers = List.of(
        createTestUser("johndoe", "John", "Doe", "john.doe@example.com", UserRole.USER, UserStatus.ACTIVE),
        createTestUser("jane.smith", "Jane", "Smith", "jane.smith@example.com", UserRole.USER, UserStatus.ACTIVE),
        // ... more users
    );
    userRepository.saveAll(testUsers);
}
```

### 7.3 Тестовые константы

**Используйте фиксированные UUID для предсказуемости:**
```java
private UUID testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
private UUID tweetId = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
```

**Или используйте UUID.randomUUID() для уникальности:**
```java
private UUID testUserId = UUID.randomUUID();
```

**Выбор зависит от контекста:**
- Фиксированные UUID - когда нужна предсказуемость
- randomUUID() - когда уникальность важнее

---

## 8. Паттерн AAA (Arrange-Act-Assert)

### 8.1 Структура теста

**Всегда следуйте паттерну AAA:**

```java
@Test
void createTweet_WithValidData_ShouldReturnTweetResponseDto() {
    // Arrange (Given) - подготовка данных
    doNothing().when(tweetValidator).validateForCreate(validRequestDto);
    when(tweetMapper.toEntity(validRequestDto)).thenReturn(mappedTweet);
    when(tweetRepository.saveAndFlush(mappedTweet)).thenReturn(savedTweet);
    when(tweetMapper.toResponseDto(savedTweet)).thenReturn(responseDto);

    // Act (When) - выполнение действия
    TweetResponseDto result = tweetService.createTweet(validRequestDto);

    // Assert (Then) - проверка результата
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(savedTweet.getId());
    assertThat(result.userId()).isEqualTo(testUserId);
    assertThat(result.content()).isEqualTo("Hello World");
}
```

### 8.2 Комментарии в тестах

**Комментарии не нужны**:

```java
@Test
void getUserById_WhenUserExists_ShouldReturnUser() {
    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toUserResponseDto(testUser)).thenReturn(testUserResponseDto);
    
    Optional<UserResponseDto> result = userService.getUserById(testUserId);
    
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(testUserResponseDto);
    verify(userRepository).findById(testUserId);
    verify(userMapper).toUserResponseDto(testUser);
}
```

---

## 9. Интеграционные тесты

### 9.1 BaseIntegrationTest

**Используйте базовый класс для интеграционных тестов:**

```java
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class TweetControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
}
```

### 9.2 MockMvc для тестирования REST endpoints

**Пример теста контроллера:**
```java
@Test
void createTweet_WithValidData_ShouldReturn201Created() throws Exception {
    String content = "Hello World";
    setupUserExistsStub(testUserId, true);
    CreateTweetRequestDto request = createValidRequest(testUserId, content);

    String responseJson = mockMvc.perform(post("/api/v1/tweets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.userId").value(testUserId.toString()))
        .andExpect(jsonPath("$.content").value(content))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists())
        .andReturn()
        .getResponse()
        .getContentAsString();

    TweetResponseDto responseDto = objectMapper.readValue(responseJson, TweetResponseDto.class);

    assertThat(verifyTweetInDatabase(responseDto.id())).isTrue();
    assertThat(getTweetCount()).isEqualTo(1);
}
```

### 9.3 WireMock для внешних сервисов

**Используйте helper методы из BaseIntegrationTest:**
```java
@Test
void createTweet_WhenUsersApiReturns500_ShouldHandleGracefully() throws Exception {
    CreateTweetRequestDto request = createValidRequest(testUserId, "Valid content");
    setupUserExistsStubWithError(testUserId, 500);

    mockMvc.perform(post("/api/v1/tweets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.type").exists());

    assertThat(getTweetCount()).isEqualTo(0);
}
```

### 9.4 @Transactional для изоляции

**Используйте @Transactional для автоматической откатки:**
```java
@SpringBootTest
@Transactional
public class UserControllerTest extends BaseIntegrationTest {
    // Каждый тест будет выполнен в транзакции, которая откатится после теста
}
```

---

## 10. Тестирование мапперов

### 10.1 MapStruct мапперы

**Используйте реальный маппер (не мок):**
```java
class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Nested
    class ToUserTest {
        @Test
        void toUser_WithValidData_ShouldMapCorrectly() {
            UserRequestDto userRequestDto = new UserRequestDto(
                "testuser",
                "John",
                "Doe",
                "john.doe@example.com",
                "password123"
            );

            User result = userMapper.toUser(userRequestDto);

            assertThat(result).isNotNull();
            assertThat(result.getLogin()).isEqualTo("testuser");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(result.getPasswordHash()).isNull();
            assertThat(result.getStatus()).isNull();
            assertThat(result.getRole()).isNull();
            assertThat(result.getId()).isNull();
        }
    }
}
```

### 10.2 Проверка игнорируемых полей

**Убедитесь, что технические поля игнорируются:**
```java
@Test
void toUser_ShouldIgnorePasswordHashField() {
    UserRequestDto userRequestDto = new UserRequestDto(
        "testuser",
        "Test",
        "User",
        "test@example.com",
        "password123"
    );

    User result = userMapper.toUser(userRequestDto);

    assertThat(result).isNotNull();
    assertThat(result.getPasswordHash()).isNull();
}

@Test
void toUser_ShouldNotSetId() {
    UserRequestDto userRequestDto = new UserRequestDto(
        "testuser",
        "Test",
        "User",
        "test@example.com",
        "password123"
    );

    User result = userMapper.toUser(userRequestDto);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNull();
}
```

---

## 11. Тестирование валидаторов

### 11.1 Использование @Spy для Bean Validation

**Используйте реальный Validator с @Spy:**
```java
@ExtendWith(MockitoExtension.class)
class TweetValidatorImplTest {

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private UserGateway userGateway;

    @InjectMocks
    private TweetValidatorImpl tweetValidator;

    @Test
    void validateForCreate_WhenValidData_ShouldCompleteWithoutExceptions() {
        UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String validContent = "This is a valid tweet content";
        CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
            .content(validContent)
            .userId(validUserId)
            .build();

        when(userGateway.existsUser(validUserId)).thenReturn(true);

        tweetValidator.validateForCreate(requestDto);

        verify(userGateway, times(1)).existsUser(validUserId);
    }
}
```

### 11.2 Проверка исключений валидации

**Проверяйте тип и содержимое исключений:**
```java
@Test
void validateForCreate_WhenContentIsEmptyString_ShouldThrowFormatValidationException() {
    UUID validUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    CreateTweetRequestDto requestDto = CreateTweetRequestDto.builder()
        .content("")
        .userId(validUserId)
        .build();

    assertThatThrownBy(() -> tweetValidator.validateForCreate(requestDto))
        .isInstanceOf(FormatValidationException.class)
        .satisfies(exception -> {
            FormatValidationException ex = (FormatValidationException) exception;
            assertThat(ex.getConstraintName()).isEqualTo("CONTENT_VALIDATION");
        });

    verify(userGateway, never()).existsUser(any());
}
```

---

## 12. Тестирование утилитарных классов

### 12.1 Простые unit тесты

**Для утилитарных классов используйте простые тесты:**
```java
class PasswordUtilTest {

    @Nested
    class GetSaltTests {
        @Test
        void shouldReturnNonNullSaltArray() {
            byte[] salt = PasswordUtil.getSalt();
            assertNotNull(salt);
        }

        @Test
        void shouldReturnSaltArrayWithCorrectLength() {
            byte[] salt = PasswordUtil.getSalt();
            assertEquals(16, salt.length);
        }

        @Test
        void shouldReturnDifferentSaltValuesOnMultipleCalls() {
            Set<String> saltValues = new HashSet<>();

            for (int i = 0; i < 100; i++) {
                byte[] salt = PasswordUtil.getSalt();
                String saltString = Arrays.toString(salt);
                saltValues.add(saltString);
            }

            assertTrue(saltValues.size() > 1, "All salt values should be different");
        }
    }
}
```

---

## 13. Покрытие тестами

### 13.1 Что тестировать

**Обязательно тестируйте:**
- ✅ Успешные сценарии (happy path)
- ✅ Ошибочные сценарии (validation errors, business rule violations)
- ✅ Исключения и их типы
- ✅ Взаимодействие с зависимостями (verify calls)

**Не обязательно тестировать:**
- ❌ Геттеры и сеттеры (если они простые)
- ❌ Lombok-генерируемый код
- ❌ MapStruct-генерируемый код (но проверяйте результаты маппинга)
- ❌ Spring-конфигурация (если она тривиальна)

### 13.2 Примеры покрытия

**Для метода createUser тестируйте:**
```java
// Успешный сценарий
@Test
void createUser_WithValidData_ShouldCreateAndReturnUser()

// Ошибочные сценарии
@Test
void createUser_WhenLoginExists_ShouldThrowUniquenessValidationException()
@Test
void createUser_WhenEmailExists_ShouldThrowUniquenessValidationException()

// Проверка побочных эффектов
@Test
void createUser_ShouldSetStatusToActive()
@Test
void createUser_ShouldSetRoleToUser()
@Test
void createUser_ShouldHashPassword()
@Test
void createUser_ShouldSetCreatedAtTimestamp()
```

---

## 14. Best Practices

### 14.1 Изоляция тестов

**Каждый тест должен быть независимым:**
- Не полагайтесь на порядок выполнения тестов
- Не используйте общее состояние между тестами
- Используйте `@BeforeEach` для инициализации, а не `@BeforeAll`

### 14.2 Один assertion на тест (когда возможно)

**Предпочтительно:**
```java
@Test
void getUserById_WhenUserExists_ShouldReturnUser() {
    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(userMapper.toUserResponseDto(testUser)).thenReturn(testUserResponseDto);

    Optional<UserResponseDto> result = userService.getUserById(testUserId);

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(testUserResponseDto);
}
```

**Но можно использовать несколько assertions для проверки разных аспектов:**
```java
@Test
void createUser_WithValidData_ShouldCreateAndReturnUser() {
    // ... setup ...

    UserResponseDto result = userService.createUser(testUserRequestDto);

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(testUserResponseDto);
    assertThat(result.id()).isEqualTo(savedUser.getId());
    assertThat(result.login()).isEqualTo("testuser");
    assertThat(result.firstName()).isEqualTo("Test");
    assertThat(result.email()).isEqualTo("test@example.com");
    assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
    assertThat(result.role()).isEqualTo(UserRole.USER);
}
```

### 14.3 Тестовые данные

**Используйте реалистичные данные:**
```java
// Хорошо
String email = "john.doe@example.com";
String login = "johndoe";

// Плохо
String email = "test@test.com";
String login = "test";
```

### 14.4 Избегайте магических чисел

**Используйте константы или переменные:**
```java
// Хорошо
private static final int MAX_CONTENT_LENGTH = 280;
String content = "A".repeat(MAX_CONTENT_LENGTH + 1);

// Плохо
String content = "A".repeat(281);
```

### 14.5 Проверка взаимодействий

**Всегда проверяйте важные взаимодействия:**
```java
@Test
void createTweet_WithValidData_ShouldCallEachDependencyExactlyOnce() {
    // ... setup ...

    tweetService.createTweet(validRequestDto);

    verify(tweetValidator, times(1)).validateForCreate(eq(validRequestDto));
    verify(tweetMapper, times(1)).toEntity(eq(validRequestDto));
    verify(tweetRepository, times(1)).saveAndFlush(eq(mappedTweet));
    verify(tweetMapper, times(1)).toResponseDto(eq(savedTweet));
}
```

### 14.6 Проверка отсутствия вызовов

**Проверяйте, что методы не вызывались:**
```java
@Test
void createUser_WhenLoginExists_ShouldThrowUniquenessValidationException() {
    doThrow(new UniquenessValidationException("login", "testuser"))
        .when(userValidator).validateForCreate(testUserRequestDto);

    assertThatThrownBy(() -> userService.createUser(testUserRequestDto))
        .isInstanceOf(UniquenessValidationException.class);

    verify(userValidator).validateForCreate(testUserRequestDto);
    verify(userMapper, never()).toUser(any());
    verify(userRepository, never()).saveAndFlush(any());
    verify(userMapper, never()).toUserResponseDto(any());
}
```

---

## 15. Зависимости для тестирования

### 15.1 Обязательные зависимости

```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.junit.jupiter:junit-jupiter-api'
testImplementation 'org.junit.jupiter:junit-jupiter-engine'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
testImplementation 'org.mockito:mockito-core'
testImplementation 'org.mockito:mockito-junit-jupiter'
testImplementation 'org.assertj:assertj-core'
```

### 15.2 Для интеграционных тестов

```gradle
testImplementation 'org.testcontainers:junit-jupiter'
testImplementation 'org.testcontainers:postgresql'
testImplementation 'com.github.tomakehurst:wiremock-jre8'
```

---

## 16. Чеклист перед коммитом

Перед коммитом тестов убедитесь:

- [ ] Все тесты проходят
- [ ] Тесты следуют стандарту именования
- [ ] Используется правильный тип теста (unit vs integration)
- [ ] Тесты изолированы и независимы
- [ ] Покрыты успешные и ошибочные сценарии
- [ ] Проверены граничные случаи
- [ ] Используются правильные assertions (AssertJ предпочтительно)
- [ ] Проверены взаимодействия с зависимостями (verify)
- [ ] Тестовые данные реалистичны
- [ ] Используется @Nested для группировки (когда уместно)
- [ ] Helper методы созданы для повторяющегося кода

---

## 17. Примеры из проекта

### 17.1 Unit тест сервиса

См. `services/users-api/src/test/java/com/twitter/service/UserServiceImplTest.java`

### 17.2 Integration тест контроллера

См. `services/users-api/src/test/java/com/twitter/controller/UserControllerTest.java`

### 17.3 Тест маппера

См. `services/users-api/src/test/java/com/twitter/mapper/UserMapperTest.java`

### 17.4 Тест валидатора

См. `services/users-api/src/test/java/com/twitter/validation/UserValidatorImplTest.java`

### 17.5 Тест утилиты

См. `services/users-api/src/test/java/com/twitter/util/PasswordUtilTest.java`

---

## 18. Версия истории

- **v1.0** (2025-01-27): Первая версия на основе анализа тестов из users-api и tweet-api

---

## Ссылки

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Testcontainers Documentation](https://www.testcontainers.org/)

