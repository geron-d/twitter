# JavaDoc Translation Guidelines and Terminology

## Overview
This document defines the translation guidelines and terminology standards for converting Russian JavaDoc to English in the Twitter Users-API project. It ensures consistency, professionalism, and technical accuracy across all documentation.

## Translation Principles

### 1. Language Style
- **Tone**: Professional, technical, clear, and concise
- **Voice**: Use active voice when possible, passive voice when appropriate
- **Tense**: Present tense for descriptions, imperative for instructions
- **Person**: Third person for descriptions, second person for instructions

### 2. Technical Writing Standards
- Use standard JavaDoc conventions
- Follow Oracle JavaDoc style guide
- Maintain consistency with Spring Framework documentation style
- Use industry-standard terminology

### 3. Translation Approach
- **Meaning over literal translation**: Preserve technical meaning rather than word-for-word translation
- **Context-aware**: Consider the technical context and domain knowledge
- **Consistent terminology**: Use the same English terms for the same Russian concepts
- **Professional tone**: Maintain formal, technical writing style

## Terminology Dictionary

### Core Concepts
| Russian | English | Context/Notes |
|---------|---------|---------------|
| пользователь | user | System user, application user |
| система | system | Twitter system, application system |
| сервис | service | Business logic service, Spring service |
| контроллер | controller | REST controller, Spring controller |
| репозиторий | repository | Data access layer, JPA repository |
| валидация | validation | Data validation, business rule validation |
| валидатор | validator | Validation component, validator class |
| маппер | mapper | Object mapping, MapStruct mapper |
| сущность | entity | JPA entity, database entity |
| запрос | request | HTTP request, API request |
| ответ | response | HTTP response, API response |
| обновление | update | Data update, record update |
| создание | create | Data creation, record creation |
| удаление | delete | Data deletion, record deletion |
| поиск | search/find | Data retrieval, record search |
| фильтрация | filtering | Data filtering, search filtering |
| пагинация | pagination | Data pagination, result pagination |
| сортировка | sorting | Data sorting, result sorting |

### Technical Terms
| Russian | English | Context/Notes |
|---------|---------|---------------|
| бизнес-логика | business logic | Core business rules and operations |
| бизнес-правила | business rules | Domain-specific validation rules |
| уникальность | uniqueness | Data uniqueness constraints |
| конфликт | conflict | Data conflict, constraint violation |
| ограничения | constraints | Validation constraints, business constraints |
| проверка | validation/check | Data validation, business rule check |
| обработка | processing | Data processing, request processing |
| обработчик | handler | Exception handler, request handler |
| исключение | exception | Java exception, error condition |
| ошибка | error | System error, validation error |
| предупреждение | warning | System warning, validation warning |
| сообщение | message | Error message, log message |
| логирование | logging | Application logging, debug logging |
| отладка | debugging | Application debugging, troubleshooting |

### Data and Security Terms
| Russian | English | Context/Notes |
|---------|---------|---------------|
| пароль | password | User password, authentication password |
| хеширование | hashing | Password hashing, cryptographic hashing |
| соль | salt | Cryptographic salt, password salt |
| шифрование | encryption | Data encryption, cryptographic encryption |
| безопасность | security | Data security, application security |
| аутентификация | authentication | User authentication, login authentication |
| авторизация | authorization | Access control, permission authorization |
| роль | role | User role, access role |
| права | permissions | Access permissions, user permissions |
| статус | status | User status, record status |
| состояние | state | Object state, system state |

### API and HTTP Terms
| Russian | English | Context/Notes |
|---------|---------|---------------|
| эндпоинт | endpoint | REST endpoint, API endpoint |
| маршрут | route | URL route, request route |
| параметр | parameter | Method parameter, request parameter |
| заголовок | header | HTTP header, request header |
| тело | body | Request body, response body |
| код ответа | response code | HTTP status code, response status |
| метод | method | HTTP method, REST method |
| ресурс | resource | REST resource, API resource |
| операция | operation | CRUD operation, business operation |
| транзакция | transaction | Database transaction, business transaction |

### Database Terms
| Russian | English | Context/Notes |
|---------|---------|---------------|
| база данных | database | Application database, data storage |
| таблица | table | Database table, data table |
| запись | record | Database record, data record |
| поле | field | Database field, object field |
| колонка | column | Database column, table column |
| индекс | index | Database index, search index |
| связь | relationship | Database relationship, entity relationship |
| внешний ключ | foreign key | Database foreign key, reference key |
| первичный ключ | primary key | Database primary key, unique identifier |
| ограничение | constraint | Database constraint, table constraint |

## JavaDoc Tag Guidelines

### Required Tags
- `@author Twitter Team` - Always use "Twitter Team" as author
- `@version 1.0` - Use version 1.0 for initial documentation
- `@since 2025-01-27` - Use current date for new documentation
- `@param` - Describe each parameter clearly and concisely
- `@return` - Describe return value and possible states
- `@throws` - Document all possible exceptions with conditions
- `@see` - Add cross-references to related classes/methods

### Tag Content Guidelines
- **Parameter descriptions**: Start with article (a, an, the) when appropriate
- **Return descriptions**: Use "the" for specific return values
- **Exception descriptions**: Use "if" or "when" to describe conditions
- **Cross-references**: Use full class names with method signatures

## Style Guidelines

### Sentence Structure
- Use complete sentences for descriptions
- Start with capital letter, end with period
- Use present tense for descriptions
- Use imperative mood for instructions

### Technical Descriptions
- Be specific and precise
- Avoid ambiguous terms
- Use technical terminology consistently
- Provide context when necessary

### Code Examples
- Use `{@code}` tags for inline code
- Use `<pre>{@code ... }</pre>` for multi-line examples
- Ensure examples are syntactically correct
- Include relevant imports in examples

## Common Translation Patterns

### Method Descriptions
- **Russian**: "Получает пользователя по идентификатору"
- **English**: "Retrieves a user by their identifier"
- **Pattern**: [Action] + [object] + [by/from/with] + [condition]

### Class Descriptions
- **Russian**: "Сервис для управления пользователями"
- **English**: "Service for user management"
- **Pattern**: [Type] + [for] + [purpose/functionality]

### Exception Descriptions
- **Russian**: "Исключение при нарушении валидации"
- **English**: "Exception thrown when validation fails"
- **Pattern**: [Exception] + [thrown when] + [condition]

## Quality Checklist

### Before Translation
- [ ] Understand the technical context
- [ ] Identify key technical terms
- [ ] Check for existing English terminology
- [ ] Plan the translation approach

### During Translation
- [ ] Use consistent terminology
- [ ] Maintain technical accuracy
- [ ] Follow JavaDoc conventions
- [ ] Preserve original meaning
- [ ] Use professional tone

### After Translation
- [ ] Review for consistency
- [ ] Check technical accuracy
- [ ] Validate JavaDoc syntax
- [ ] Ensure completeness
- [ ] Test readability

## Examples

### Class Documentation
```java
/**
 * Service for user management in Twitter microservices.
 * 
 * This service provides business logic for CRUD operations with users,
 * including validation, password hashing, and business rule enforcement.
 * It integrates with the user repository and follows Twitter's security
 * standards for user data handling.
 * 
 * @author Twitter Team
 * @version 1.0
 * @since 2025-01-27
 * @see UserRepository for data access operations
 * @see UserValidator for validation logic
 */
```

### Method Documentation
```java
/**
 * Retrieves a user by their unique identifier.
 * 
 * This method performs a database lookup and returns the user data
 * if found. The method returns an empty Optional if the user
 * does not exist or has been deactivated.
 * 
 * @param id the unique identifier of the user
 * @return Optional containing user data or empty if not found
 * @throws IllegalArgumentException if the ID is null
 * @see UserRepository#findById(Object) for underlying data access
 * @since 2025-01-27
 */
```

### Exception Documentation
```java
/**
 * Exception thrown when user validation fails.
 * 
 * This exception is thrown when user data violates business rules
 * or validation constraints. It provides detailed information about
 * the validation failure to help with error resolution.
 * 
 * @author Twitter Team
 * @version 1.0
 * @since 2025-01-27
 * @see UserValidator for validation logic
 * @see GlobalExceptionHandler for error handling
 */
```

## Version History
- **v1.0** (2025-01-27): Initial version with comprehensive translation guidelines and terminology dictionary
