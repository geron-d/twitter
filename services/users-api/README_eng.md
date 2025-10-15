# Users API Service

## Introduction

**Users API** is a microservice for user management in the Twitter system, built on Java 24 and Spring Boot 3. The service provides a REST API for creating, reading, updating, and deactivating users with support for role-based access control.

### Key Features:
- ✅ CRUD operations for users
- ✅ Role-based model (USER, ADMIN, MODERATOR)
- ✅ Secure password hashing
- ✅ Data validation
- ✅ Pagination and filtering
- ✅ Request logging
- ✅ Protection against deleting the last administrator
- ✅ OpenAPI/Swagger documentation

## Architecture

### Package Structure

```
com.twitter/
├── Application.java              # Main application class
├── controller/
│   └── UserController.java       # REST controller
├── dto/
│   ├── UserRequestDto.java       # DTO for user creation
│   ├── UserResponseDto.java      # DTO for response
│   ├── UserUpdateDto.java        # DTO for update
│   ├── UserPatchDto.java         # DTO for partial update
│   ├── UserRoleUpdateDto.java    # DTO for role update
│   └── filter/
│       └── UserFilter.java       # Search filter
├── entity/
│   └── User.java                 # JPA entity
├── enums/
│   ├── UserRole.java             # User roles
│   └── UserStatus.java           # User statuses
├── exception/
│   └── validation/               # Validation exceptions
│       ├── ValidationException.java           # Base exception
│       ├── UniquenessValidationException.java # Uniqueness errors
│       ├── BusinessRuleValidationException.java # Business rules
│       └── FormatValidationException.java     # Data format
├── mapper/
│   └── UserMapper.java           # MapStruct mapper
├── repository/
│   └── UserRepository.java       # JPA repository
├── service/
│   ├── UserService.java          # Service interface
│   └── UserServiceImpl.java      # Service implementation
├── util/
│   ├── PasswordUtil.java         # Password utilities
│   └── PatchDtoFactory.java      # Factory for PATCH operations
└── validation/
    ├── UserValidator.java         # Validator interface
    └── UserValidatorImpl.java     # Validator implementation
```

### Component Diagram

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   UserController│────│   UserService   │────│  UserRepository │
│                 │    │                 │    │                 │
│ - REST Endpoints│    │ - Business Logic│    │ - Data Access   │
│ - Error Handling│    │ - Orchestration │    │ - Queries       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                       │
         │──────────────────────│                       │
         ▼                      ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   UserValidator │    │   UserMapper    │    │   PostgreSQL    │
│                 │    │                 │    │                 │
│ - Data Validation│   │ - Entity Mapping│    │ - Database      │
│ - Business Rules│    │ - DTO Conversion│    │ - Tables        │
│ - Uniqueness    │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌─────────────────┐
│   Validation    │    │      DTOs       │
│   Exceptions    │    │                 │
│                 │    │ - Request/Response│
│ - Uniqueness    │    │ - Validation    │
│ - Business Rules│    │ - Constraints   │
│ - Format Errors │    │                 │
└─────────────────┘    └─────────────────┘
```

## REST API

### Base URL
```
http://localhost:8081/api/v1/users
```

### Endpoints

| Method | Path | Description | Parameters | Request Body | Response |
|--------|------|-------------|------------|--------------|----------|
| `GET` | `/{id}` | Get user by ID | `id` (UUID) | - | `UserResponseDto` |
| `GET` | `/` | Get user list | `UserFilter`, `Pageable` | - | `PagedModel<UserResponseDto>` |
| `POST` | `/` | Create new user | - | `UserRequestDto` | `UserResponseDto` |
| `PUT` | `/{id}` | Full user update | `id` (UUID) | `UserUpdateDto` | `UserResponseDto` |
| `PATCH` | `/{id}` | Partial user update | `id` (UUID) | `JsonNode` | `UserResponseDto` |
| `PATCH` | `/{id}/inactivate` | Deactivate user | `id` (UUID) | - | `UserResponseDto` |
| `PATCH` | `/{id}/role` | Update user role | `id` (UUID) | `UserRoleUpdateDto` | `UserResponseDto` |

### Detailed Endpoint Descriptions

#### 1. Get User by ID
```http
GET /api/v1/users/{id}
```

**Parameters:**
- `id` (UUID) - user identifier

**Responses:**
- `200 OK` - user found
- `404 Not Found` - user not found

**Example Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "login": "john_doe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "status": "ACTIVE",
  "role": "USER",
  "createdAt": "2025-01-21T20:30:00"
}
```

#### 2. Get User List
```http
GET /api/v1/users?firstNameContains=John&role=USER&page=0&size=10&sort=login,asc
```

**Query Parameters:**
- `firstNameContains` (String, optional) - filter by first name
- `lastNameContains` (String, optional) - filter by last name
- `email` (String, optional) - filter by email
- `login` (String, optional) - filter by login
- `role` (UserRole, optional) - filter by role
- `page` (int, default: 0) - page number
- `size` (int, default: 10) - page size (maximum: 100)
- `sort` (String, optional) - sorting

**Pagination Constraints:**
- Default page size: 10 elements
- Maximum page size: 100 elements
- When maximum size is exceeded, limit is automatically applied

**PagedModel Response Structure:**
- `content` - array of `UserResponseDto` objects
- `page` - pagination metadata:
  - `size` - page size
  - `number` - page number (starting from 0)
  - `totalElements` - total number of elements
  - `totalPages` - total number of pages

**Example Response:**
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "login": "john_doe",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "status": "ACTIVE",
      "role": "USER",
      "createdAt": "2025-01-21T20:30:00"
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

#### 3. Create User
```http
POST /api/v1/users
Content-Type: application/json
```

**Request Body:**
```json
{
  "login": "jane_smith",
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@example.com",
  "password": "securePassword123"
}
```

**Validation:**
- `login` - required, 3-50 characters
- `email` - required, valid email
- `password` - required, minimum 8 characters

**Responses:**
- `200 OK` - user created
- `400 Bad Request` - validation error
- `409 Conflict` - user with such login/email already exists

#### 4. Update User (PUT)
```http
PUT /api/v1/users/{id}
Content-Type: application/json
```

**Request Body:**
```json
{
  "login": "jane_smith_updated",
  "firstName": "Jane",
  "lastName": "Smith-Wilson",
  "email": "jane.wilson@example.com",
  "password": "newSecurePassword123"
}
```

**Responses:**
- `200 OK` - user updated
- `404 Not Found` - user not found
- `400 Bad Request` - validation error
- `409 Conflict` - uniqueness conflict

#### 5. Partial User Update (PATCH)
```http
PATCH /api/v1/users/{id}
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith-Wilson"
}
```

**Responses:**
- `200 OK` - user updated
- `404 Not Found` - user not found
- `400 Bad Request` - validation error or incorrect JSON Patch
- `409 Conflict` - login/email uniqueness conflict

#### 6. Deactivate User
```http
PATCH /api/v1/users/{id}/inactivate
```

**Responses:**
- `200 OK` - user deactivated
- `404 Not Found` - user not found
- `400 Bad Request` - attempt to deactivate the last administrator

#### 7. Update User Role
```http
PATCH /api/v1/users/{id}/role
Content-Type: application/json
```

**Request Body:**
```json
{
  "role": "ADMIN"
}
```

**Responses:**
- `200 OK` - role updated
- `404 Not Found` - user not found
- `400 Bad Request` - attempt to change the last administrator's role

## OpenAPI/Swagger Documentation

### Overview

The service includes complete OpenAPI 3.0 documentation provided through SpringDoc OpenAPI. The documentation contains interactive capabilities for API testing, detailed data schemas, and request/response examples.

### Documentation Access

#### Swagger UI
- **URL**: `http://localhost:8081/swagger-ui.html`
- **Description**: Interactive interface for exploring and testing the API
- **Features**:
  - View all endpoints with detailed descriptions
  - Interactive API testing (Try it out)
  - View data schemas and validation
  - Request and response examples
  - Client code auto-generation

#### OpenAPI Specification
- **URL**: `http://localhost:8081/v3/api-docs`
- **Description**: OpenAPI 3.0 JSON specification
- **Usage**:
  - Generate client SDKs
  - Import into API testing tools (Postman, Insomnia)
  - Integrate with CI/CD pipelines
  - Validate API contracts

#### Swagger Configuration
- **URL**: `http://localhost:8081/v3/api-docs/swagger-config`
- **Description**: Swagger UI configuration

### Documentation Features

#### Interactive Capabilities
- **Try it out**: Test all endpoints directly in the browser
- **Request Builder**: Visual interface for creating requests
- **Response Viewer**: Display responses with formatting
- **Schema Explorer**: View and understand data models

#### Complete API Coverage
- **All Endpoints**: Documentation of all 7 API endpoints
- **Data Models**: Detailed schemas for all DTOs and entities
- **Error Handling**: Documentation of all error scenarios and responses
- **Validation Rules**: Field-level validation requirements
- **Business Rules**: Documentation of system constraints and rules

#### Developer-Friendly Features
- **Code Generation**: Create client SDKs for various languages
- **Postman Integration**: Export API collections for testing
- **API Testing**: Built-in testing capabilities
- **Documentation Export**: Download documentation in various formats

### Configuration

OpenAPI documentation is configured through:

1. **Dependencies**: SpringDoc OpenAPI starter in `build.gradle`
2. **Configuration Class**: `OpenApiConfig.java` for API metadata
3. **Application Settings**: Swagger UI configuration in `application.yml`

### Usage Examples

#### Accessing Swagger UI
```bash
# Start application
./gradlew bootRun

# Open Swagger UI in browser
open http://localhost:8081/swagger-ui.html
```

#### Generating Client Code
```bash
# Using OpenAPI Generator
openapi-generator-cli generate \
  -i http://localhost:8081/v3/api-docs \
  -g java \
  -o ./generated-client

# Generate TypeScript client
openapi-generator-cli generate \
  -i http://localhost:8081/v3/api-docs \
  -g typescript-axios \
  -o ./generated-client-ts
```

#### Testing API through Swagger UI
1. Open Swagger UI in browser
2. Select the desired endpoint
3. Click "Try it out"
4. Fill in required parameters
5. Click "Execute"
6. View the response

### Documentation Security

#### Sensitive Data Protection
- **Passwords**: Password fields are marked as sensitive and hidden in examples
- **Authentication**: Future versions will include JWT authentication
- **Rate Limiting**: API limitations are documented in response headers
- **Data Privacy**: Personal information handling follows privacy guidelines

### Troubleshooting

#### Common Issues

1. **Swagger UI not loading**
   - Check that SpringDoc dependency is properly added
   - Ensure application is running on the correct port
   - Check browser console for JavaScript errors

2. **API endpoints not displaying**
   - Ensure controllers are properly annotated
   - Check package scanning configuration
   - Verify proper Spring Boot auto-configuration

3. **Schema validation errors**
   - Check DTO annotations and validation rules
   - Ensure proper Jackson configuration
   - Verify OpenAPI schema generation

#### Debug Mode
Enable debug logging for SpringDoc:
```yaml
logging:
  level:
    org.springdoc: DEBUG
```

### Future Improvements

- **Authentication**: JWT token integration
- **Rate Limiting**: API rate limiting policy documentation
- **Webhooks**: Event-driven API documentation
- **Versioning**: Multi-version API support
- **Internationalization**: Multi-language documentation support

### Support

For API documentation issues:
- Check application logs for errors
- Review OpenAPI specification at `/v3/api-docs`
- Contact the development team for assistance

## Business Logic

### UserService

The main service for working with users, implementing the following operations:

#### Service Methods:

1. **`getUserById(UUID id)`**
   - Gets user by identifier
   - Returns `Optional<UserResponseDto>`
   - Logic: repository search and DTO mapping

2. **`findAll(UserFilter userFilter, Pageable pageable)`**
   - Gets user list with filtering and pagination
   - Returns `Page<UserResponseDto>`
   - Logic: building specification from filter and mapping results

3. **`createUser(UserRequestDto userRequest)`**
   - Creates new user
   - Returns `UserResponseDto`
   - Logic:
     - Validate login and email uniqueness
     - Map DTO to entity
     - Set ACTIVE status and USER role
     - Hash password
     - Save to database

4. **`updateUser(UUID id, UserUpdateDto userDetails)`**
   - Full user update
   - Returns `Optional<UserResponseDto>`
   - Logic:
     - Find user by ID
     - Validate uniqueness (excluding current user)
     - Update fields through mapper
     - Hash new password (if specified)
     - Save changes

5. **`patchUser(UUID id, JsonNode patchNode)`**
   - Partial user update
   - Returns `Optional<UserResponseDto>`
   - Logic:
     - Find user by ID
     - Apply JSON Patch to DTO
     - Validate result
     - Check uniqueness
     - Update entity

6. **`inactivateUser(UUID id)`**
   - User deactivation
   - Returns `Optional<UserResponseDto>`
   - Logic:
     - Check that this is not the last active administrator
     - Set INACTIVE status
     - Log operation

7. **`updateUserRole(UUID id, UserRoleUpdateDto roleUpdate)`**
   - Update user role
   - Returns `Optional<UserResponseDto>`
   - Logic:
     - Check that the last administrator's role cannot be changed
     - Update role
     - Log change

### Key Business Rules:

1. **Data Uniqueness:**
   - Login must be unique
   - Email must be unique
   - When updating, current user is excluded

2. **Administrator Protection:**
   - Cannot deactivate the last active administrator
   - Cannot change the last active administrator's role

3. **Password Security:**
   - Passwords are hashed using PBKDF2
   - Cryptographically secure salt is used
   - Passwords are never returned in responses

4. **Data Validation:**
   - All incoming data is validated
   - Jakarta Validation is used
   - Custom validation for business rules

## Validation Layer

### Validation Architecture

The service uses a centralized validation layer through `UserValidator`, which provides:

- **Consistent validation** for all user operations
- **Separation of concerns** between business logic and validation
- **Typed exceptions** for different types of errors
- **Integration with Jakarta Validation** for data format checking

### UserValidator

The `UserValidator` interface defines validation methods for all user operations. It includes methods for validating creation, updates, partial updates, uniqueness checking, admin deactivation, and role changes.

### Validation Exception Types

#### 1. ValidationException (base exception)
Base abstract exception for all types of validation errors. Contains information about validation type and error context.

#### 2. UniquenessValidationException
Used for login and email uniqueness errors. Contains information about the field that violates uniqueness and its value.

**HTTP Status:** `409 Conflict`  
**Content-Type:** `application/problem+json`

**Example Response:**
```json
{
  "title": "Uniqueness Validation Error",
  "detail": "User with login 'testuser' already exists",
  "fieldName": "login",
  "fieldValue": "testuser",
  "validationType": "UNIQUENESS",
  "timestamp": "2025-01-21T19:30:00Z"
}
```

#### 3. BusinessRuleValidationException
Used for business rule violations, such as attempting to deactivate the last administrator or change their role. Contains the name of the violated rule.

**HTTP Status:** `409 Conflict`  
**Content-Type:** `application/problem+json`

**Example Response:**
```json
{
  "title": "Business Rule Validation Error",
  "detail": "Business rule 'LAST_ADMIN_DEACTIVATION' violated for context: userId=123e4567-e89b-12d3-a456-426614174000",
  "ruleName": "LAST_ADMIN_DEACTIVATION",
  "validationType": "BUSINESS_RULE",
  "timestamp": "2025-01-21T19:30:00Z"
}
```

#### 4. FormatValidationException
Used for data format errors, such as incorrect login length, invalid email, or other Jakarta Validation constraint violations.

**HTTP Status:** `400 Bad Request`  
**Content-Type:** `application/problem+json`

**Example Response:**
```json
{
  "title": "Format Validation Error",
  "detail": "Validation failed for field 'login': size must be between 3 and 50",
  "validationType": "FORMAT",
  "timestamp": "2025-01-21T19:30:00Z"
}
```

### Validation by Operations

#### User Creation (CREATE)
Login and email uniqueness is checked among all existing users. When duplication is detected, `UniquenessValidationException` is thrown.

#### User Update (UPDATE)
Login and email uniqueness is checked among all users, excluding the current user being updated. This allows the user to keep their current data when updating other fields.

#### Partial Update (PATCH)
Two-stage validation is performed: first, JSON Patch structure is validated, then Bean Validation is applied to the resulting DTO. Additionally, uniqueness is checked only for fields that are being changed.

#### User Deactivation (INACTIVATE)
Business rule is checked: the last active administrator in the system cannot be deactivated. When attempting to deactivate the last admin, `BusinessRuleValidationException` is thrown.

#### Role Change (ROLE_CHANGE)
Business rule is checked: the last active administrator's role cannot be changed to any other role. This guarantees at least one administrator in the system.

### Usage Examples

#### Creating User with Duplicate Login
```bash
curl -X POST http://localhost:8081/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "login": "existinguser",
    "email": "new@example.com",
    "password": "password123"
  }'
```

**Response (409 Conflict):**
```json
{
  "title": "Uniqueness Validation Error",
  "detail": "User with login 'existinguser' already exists",
  "fieldName": "login",
  "fieldValue": "existinguser",
  "validationType": "UNIQUENESS",
  "timestamp": "2025-01-21T19:30:00Z"
}
```

#### Attempting to Deactivate Last Admin
```bash
curl -X PATCH http://localhost:8081/api/v1/users/123e4567-e89b-12d3-a456-426614174000/inactivate
```

**Response (409 Conflict):**
```json
{
  "title": "Business Rule Validation Error",
  "detail": "Business rule 'LAST_ADMIN_DEACTIVATION' violated for context: userId=123e4567-e89b-12d3-a456-426614174000",
  "ruleName": "LAST_ADMIN_DEACTIVATION",
  "validationType": "BUSINESS_RULE",
  "timestamp": "2025-01-21T19:30:00Z"
}
```

#### PATCH with Incorrect Data Format
```bash
curl -X PATCH http://localhost:8081/api/v1/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Content-Type: application/json" \
  -d '{"login": "ab"}'
```

**Response (400 Bad Request):**
```json
{
  "title": "Format Validation Error",
  "detail": "Validation failed for field 'login': size must be between 3 and 50",
  "validationType": "FORMAT",
  "timestamp": "2025-01-21T19:30:00Z"
}
```

## Database Operations

### User Entity

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String login;
    
    private String firstName;
    private String lastName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Column(nullable = false)
    private String passwordSalt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

### Users Table

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY, NOT NULL | Unique identifier |
| `login` | VARCHAR | UNIQUE, NOT NULL | User login |
| `first_name` | VARCHAR | NULL | First name |
| `last_name` | VARCHAR | NULL | Last name |
| `email` | VARCHAR | UNIQUE, NOT NULL | Email address |
| `password_hash` | VARCHAR | NOT NULL | Password hash |
| `password_salt` | VARCHAR | NOT NULL | Salt for hashing |
| `status` | VARCHAR | NOT NULL | Status (ACTIVE/INACTIVE) |
| `role` | VARCHAR | NOT NULL | Role (USER/ADMIN/MODERATOR) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | User creation timestamp |

### UserRepository

Repository interface extends `JpaRepository` and `JpaSpecificationExecutor`:

```java
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    long countByRoleAndStatus(UserRole role, UserStatus status);
    boolean existsByLogin(String login);
    boolean existsByEmail(String email);
    boolean existsByLoginAndIdNot(String login, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);
}
```

### Specifications for Filtering

Filtering is implemented through Spring Data JPA Specifications:

```java
public record UserFilter(String firstNameContains, String lastNameContains, 
                        String email, String login, UserRole role) {
    public Specification<User> toSpecification() {
        return firstNameContainsSpec()
            .and(lastNameContainsSpec())
            .and(emailSpec())
            .and(loginSpec())
            .and(roleSpec());
    }
}
```

## Usage Examples

### Creating User

```bash
curl -X POST http://localhost:8081/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "login": "newuser",
    "firstName": "New",
    "lastName": "User",
    "email": "newuser@example.com",
    "password": "securePassword123"
  }'
```

### Getting Users with Filtering

```bash
curl "http://localhost:8081/api/v1/users?firstNameContains=John&role=USER&page=0&size=10"
```

### Updating User

```bash
curl -X PUT http://localhost:8081/api/v1/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Content-Type: application/json" \
  -d '{
    "login": "updateduser",
    "firstName": "Updated",
    "lastName": "User",
    "email": "updated@example.com",
    "password": "newPassword123"
  }'
```

### Partial Update

```bash
curl -X PATCH http://localhost:8081/api/v1/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "New Name"
  }'
```

### Deactivating User

```bash
curl -X PATCH http://localhost:8081/api/v1/users/123e4567-e89b-12d3-a456-426614174000/inactivate
```

### Updating Role

```bash
curl -X PATCH http://localhost:8081/api/v1/users/123e4567-e89b-12d3-a456-426614174000/role \
  -H "Content-Type: application/json" \
  -d '{
    "role": "ADMIN"
  }'
```

## UML Diagrams

### Class Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        UserController                       │
├─────────────────────────────────────────────────────────────┤
│ - userService: UserService                                  │
├─────────────────────────────────────────────────────────────┤
│ + getUserById(id: UUID): ResponseEntity<UserResponseDto>   │
│ + findAll(filter: UserFilter, pageable: Pageable):         │
│   PagedModel<UserResponseDto>                              │
│ + createUser(request: UserRequestDto): UserResponseDto     │
│ + updateUser(id: UUID, details: UserUpdateDto):            │
│   ResponseEntity<UserResponseDto>                          │
│ + patchUser(id: UUID, patch: JsonNode):                    │
│   ResponseEntity<UserResponseDto>                          │
│ + inactivateUser(id: UUID): ResponseEntity<UserResponseDto>│
│ + updateUserRole(id: UUID, role: UserRoleUpdateDto):       │
│   ResponseEntity<UserResponseDto>                          │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ uses
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                        UserService                          │
├─────────────────────────────────────────────────────────────┤
│ + getUserById(id: UUID): Optional<UserResponseDto>         │
│ + findAll(filter: UserFilter, pageable: Pageable):         │
│   Page<UserResponseDto>                                     │
│ + createUser(request: UserRequestDto): UserResponseDto     │
│ + updateUser(id: UUID, details: UserUpdateDto):            │
│   Optional<UserResponseDto>                                │
│ + patchUser(id: UUID, patch: JsonNode):                    │
│   Optional<UserResponseDto>                                │
│ + inactivateUser(id: UUID): Optional<UserResponseDto>      │
│ + updateUserRole(id: UUID, role: UserRoleUpdateDto):       │
│   Optional<UserResponseDto>                                │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ implements
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      UserServiceImpl                        │
├─────────────────────────────────────────────────────────────┤
│ - objectMapper: ObjectMapper                                │
│ - userMapper: UserMapper                                    │
│ - userRepository: UserRepository                            │
│ - userValidator: UserValidator                             │
│ - patchDtoFactory: PatchDtoFactory                         │
├─────────────────────────────────────────────────────────────┤
│ + getUserById(id: UUID): Optional<UserResponseDto>         │
│ + findAll(filter: UserFilter, pageable: Pageable):         │
│   Page<UserResponseDto>                                     │
│ + createUser(request: UserRequestDto): UserResponseDto      │
│ + updateUser(id: UUID, details: UserUpdateDto):            │
│   Optional<UserResponseDto>                                │
│ + patchUser(id: UUID, patch: JsonNode):                    │
│   Optional<UserResponseDto>                                │
│ + inactivateUser(id: UUID): Optional<UserResponseDto>      │
│ + updateUserRole(id: UUID, role: UserRoleUpdateDto):       │
│   Optional<UserResponseDto>                                │
│ - setPassword(user: User, password: String): void          │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ uses
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      UserValidator                           │
├─────────────────────────────────────────────────────────────┤
│ + validateForCreate(request: UserRequestDto): void         │
│ + validateForUpdate(id: UUID, update: UserUpdateDto): void │
│ + validateForPatch(id: UUID, patch: JsonNode): void         │
│ + validateForPatchWithDto(id: UUID, patch: UserPatchDto):   │
│   void                                                      │
│ + validateUniqueness(login: String, email: String,         │
│   excludeUserId: UUID): void                               │
│ + validateAdminDeactivation(id: UUID): void                │
│ + validateRoleChange(id: UUID, newRole: UserRole): void    │
│ + validatePatchData(patch: JsonNode): void                 │
│ + validatePatchConstraints(patch: UserPatchDto): void       │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ uses
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      UserRepository                         │
├─────────────────────────────────────────────────────────────┤
│ + countByRoleAndStatus(role: UserRole, status: UserStatus):│
│   long                                                      │
│ + existsByLogin(login: String): boolean                    │
│ + existsByEmail(email: String): boolean                    │
│ + existsByLoginAndIdNot(login: String, id: UUID): boolean  │
│ + existsByEmailAndIdNot(email: String, id: UUID): boolean  │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ extends
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    JpaRepository<User, UUID>                │
│                    JpaSpecificationExecutor<User>           │
└─────────────────────────────────────────────────────────────┘
```

## Configuration

### Dependencies

Core project dependencies:

- **Spring Boot 3.x** - main framework
- **Spring Data JPA** - database operations
- **Spring Web** - REST API
- **Spring Validation** - data validation
- **SpringDoc OpenAPI** - API documentation and Swagger UI
- **MapStruct** - object mapping
- **Lombok** - code generation
- **PostgreSQL** - database
- **Micrometer Tracing** - tracing

### Dependency Management

The service uses **centralized version management** through `dependencyManagement` in the root `build.gradle`.

**Important**: When adding new dependencies to the service's `build.gradle`, **DO NOT specify versions** - they are automatically resolved through `dependencyManagement`.

## Deployment and Running

### Local Deployment

1. Ensure PostgreSQL is running on port 5432
2. Create `twitter` database
3. Start the application:

```bash
./gradlew bootRun
```

### Docker

```bash
docker build -t users-api .
docker run -p 8081:8081 users-api
```

### Monitoring

The application provides the following monitoring endpoints:

- `/actuator/health` - health status
- `/actuator/info` - application information
- `/actuator/metrics` - metrics
- `/actuator/tracing` - tracing
- `/swagger-ui.html` - interactive API documentation
- `/v3/api-docs` - OpenAPI specification

## Security

### Password Hashing

- Algorithm: PBKDF2WithHmacSHA256
- Iterations: 10,000
- Key length: 256 bits
- Salt size: 16 bytes

### Validation

- All incoming data is validated
- Jakarta Validation is used
- Custom validation for business rules

### Logging

- All requests are logged through `@LoggableRequest`
- Passwords are hidden in logs
- Detailed operation logging

## Testing

The project includes:

- **Unit tests** for all components
- **Integration tests** with TestContainers
- **Validation tests** with coverage of all scenarios
- **Security tests** for passwords
- **Exception tests** for validation

Running tests:

```bash
./gradlew test
```
