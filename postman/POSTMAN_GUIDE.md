# Postman Collection Guide

## Overview

This guide explains how to use the Twitter Users API Postman collection for API exploration and development purposes. The collection includes all API endpoints with examples and environment variables for easy testing.

## Table of Contents

- [Installation](#installation)
- [Environment Setup](#environment-setup)
- [Collection Structure](#collection-structure)
- [Using the Collection](#using-the-collection)
- [Environment Variables](#environment-variables)
- [Request Examples](#request-examples)

## Installation

### Import Collection and Environment

1. **Open Postman**
2. **Import Collection**
   - Click "Import" button
   - Select `twitter-ssers-api.postman_collection.json`
   - Click "Import"

3. **Import Environment**
   - Click "Import" button
   - Select `twitter-users-api.postman_environment.json`
   - Click "Import"

4. **Select Environment**
   - Click the environment dropdown in the top-right corner
   - Select "users-env"

## Environment Setup

### Default Environment Variables

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `baseUrl` | `http://localhost:8080` | API base URL |
| `userId` | `123e4567-e89b-12d3-a456-426614174000` | Sample user ID |
| `userFilter` | `{}` | Empty user filter |
| `pageable` | `{"page":0,"size":10}` | Default pagination |

### Environment-Specific Variables

#### Development Environment
```json
{
  "baseUrl": "http://localhost:8080"
}
```

#### Test Data Variables
```json
{
  "testUser_login": "test_user_{{timestamp}}",
  "testUser_email": "test{{timestamp}}@example.com",
  "testUser_firstName": "Test",
  "testUser_lastName": "User",
  "testUser_password": "TestPassword123!"
}
```

### Switching Environments

1. **Local Development**
   - Set `baseUrl` to `http://localhost:8080`
   - Ensure API server is running locally

## Collection Structure

### User Management Folder

The collection is organized into logical folders:

```
twitte
├── users-api/
│   ├── get user by id
│   ├── get users with filtering
│   ├── create user
│   ├── update user (complete)
│   ├── update user role
│   └── deactivate user
```

### Request Details

#### 1. get user by id
- **Method**: GET
- **URL**: `{{baseUrl}}/api/v1/users/{{userId}}`
- **Description**: Retrieves a specific user by their unique identifier
- **Examples**: user found, user not found

#### 2. get users with filtering
- **Method**: GET
- **URL**: `{{baseUrl}}/api/v1/users?userFilter={{userFilter}}&pageable={{pageable}}`
- **Description**: Retrieves paginated list of users with optional filtering
- **Examples**: paginated users response

#### 3. create user
- **Method**: POST
- **URL**: `{{baseUrl}}/api/v1/users`
- **Body**: User creation data
- **Examples**: user created, validation error, conflict error

#### 4. update user (complete)
- **Method**: PUT
- **URL**: `{{baseUrl}}/api/v1/users/{{userId}}`
- **Body**: Complete user update data
- **Examples**: user updated successfully

#### 5. update user role
- **Method**: PATCH
- **URL**: `{{baseUrl}}/api/v1/users/{{userId}}/role`
- **Body**: Role update data
- **Examples**: role updated, business rule error

#### 6. deactivate user
- **Method**: PATCH
- **URL**: `{{baseUrl}}/api/v1/users/{{userId}}/inactivate`
- **Description**: Deactivates a user by setting status to INACTIVE
- **Examples**: Success response, business rule error

## Using the Collection

### Manual API Testing

1. **Select a request** from the collection
2. **Review the request details** (method, URL, headers, body)
3. **Click "Send"** to execute the request
4. **Check the response** in the "Response" section
5. **Review example responses** for different scenarios

### Exploring API Endpoints

The collection is organized to help you explore the API systematically:

1. **Start with "get users with filtering"** to see existing users
2. **Use "create user"** to add a new user
3. **Use "get user by id"** with the created user's ID
4. **Try "update user role"** to change permissions
5. **Use "deactivate user"** to test deactivation

### Working with Examples

Each request includes multiple example responses:

- **Success responses** (200 OK) with sample data
- **Error responses** (400, 404, 409) with error details
- **Different scenarios** for testing various use cases

## Environment Variables

### Dynamic Variables

The collection uses Postman's dynamic variables for test data:

```javascript
// Timestamp for unique test data
{{$timestamp}}

// Random UUID
{{$randomUUID}}

// Random email
{{$randomEmail}}

// Random name
{{$randomFullName}}
```

### Custom Variables

#### User Filter Examples
```json
// Filter by role
{"role": "USER"}

// Filter by status
{"status": "ACTIVE"}

// Filter by email
{"email": "john.doe@example.com"}

// Combined filters
{"role": "USER", "status": "ACTIVE"}
```

#### Pagination Examples
```json
// Default pagination
{"page": 0, "size": 10}

// Large page size
{"page": 0, "size": 50}

// With sorting
{"page": 0, "size": 10, "sort": ["login,asc"]}

// Second page
{"page": 1, "size": 10}
```

## Request Examples

### Sample Request Bodies

#### Create User Request
```json
{
  "login": "jane_smith",
  "email": "jane.smith@example.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "password": "securePassword123"
}
```

#### Update User Request
```json
{
  "login": "jane_smith_updated",
  "email": "jane.wilson@example.com",
  "firstName": "Jane",
  "lastName": "Smith-Wilson",
  "password": "newSecurePassword123"
}
```

#### Update User Role Request
```json
{
  "role": "ADMIN"
}
```

### Sample Responses

#### Successful User Response
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "login": "jane_smith",
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@example.com",
  "status": "ACTIVE",
  "role": "USER"
}
```

#### Paginated Users Response
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
      "role": "USER"
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

#### Error Response Example
```json
{
  "type": "https://example.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed: email must be a valid email address",
  "timestamp": "2025-01-27T15:30:00Z"
}
```

This guide should help you effectively use the Twitter Users API Postman collection for API exploration, development, and integration reference.
