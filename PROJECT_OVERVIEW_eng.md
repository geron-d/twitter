# Twitter Microservices Project

## Project Overview

**Twitter** is a microservices platform for a social network, built on the modern technology stack of Java 24 and Spring Boot 3. The project implements a monorepo architecture with separation into independent microservices and shared libraries.

### Key Business Objectives

- **User Management**: Registration, authentication, user profiles
- **Content Management**: Creation, publication, and content moderation
- **Social Features**: Subscriptions, likes, comments, reposts
- **Moderation**: Role-based access control system for moderators and administrators
- **Analytics**: Metrics collection and analytical data processing

### Core Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 24 | Primary programming language |
| **Spring Boot** | 3.5.5 | Microservices framework |
| **Spring Data JPA** | 3.x | Database operations |
| **PostgreSQL** | 15 | Primary database |
| **Docker** | Latest | Containerization |
| **Gradle** | Latest | Build system |
| **MapStruct** | 1.6.3 | Object mapping |
| **Lombok** | 1.18.38 | Code generation |
| **Micrometer** | Latest | Monitoring and metrics |
| **TestContainers** | 1.21.3 | Integration testing |

### Dependency Management

The project uses **centralized dependency version management** through `dependencyManagement` in the root `build.gradle`.

**Managed Dependencies:**
- Spring Boot BOM (automatically)
- Lombok, MapStruct, Swagger, PostgreSQL driver
- TestContainers BOM (automatically)

## Project Architecture

### General Architecture

The project is built on **microservices architecture** principles with separation into:

- **Services** (`services/`) — independent microservices with their own business logic
- **Shared Libraries** (`shared/`) — reusable components
- **Infrastructure** — deployment and monitoring configurations

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Twitter Platform                         │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    Services Layer                           │ │
│  │  ┌─────────────────────────────────────────────────────────┐ │ │
│  │  │              Users API (Port 8081)                     │ │ │
│  │  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │ │ │
│  │  │  │ Controller  │  │  Service    │  │ Repository  │    │ │ │
│  │  │  │   Layer     │  │   Layer     │  │   Layer     │    │ │ │
│  │  │  └─────────────┘  └─────────────┘  └─────────────┘    │ │ │
│  │  └─────────────────────────────────────────────────────────┘ │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                               │                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                Shared Libraries                               │ │
│  │  ┌─────────────────┐  ┌─────────────────┐                   │ │
│  │  │   Common Lib    │  │   Database      │                   │ │
│  │  │   - Logging     │  │   - Entities    │                   │ │
│  │  │   - Exceptions  │  │   - Repos      │                   │ │
│  │  │   - AOP         │  │   - Configs    │                   │ │
│  │  └─────────────────┘  └─────────────────┘                   │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                               │                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                Infrastructure Layer                          │ │
│  │  ┌─────────────────┐  ┌─────────────────┐                   │ │
│  │  │   PostgreSQL    │  │   Docker        │                   │ │
│  │  │   (Port 5432)   │  │   Compose       │                   │ │
│  │  └─────────────────┘  └─────────────────┘                   │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Service Interaction

**Current Implementation:**
- **REST API** — synchronous interaction via HTTP
- **Database** — each service has access to shared PostgreSQL
- **Shared Libraries** — static linking through Gradle

**Planned Development:**
- **gRPC** — for high-performance inter-service communication
- **Apache Kafka** — for asynchronous event processing
- **Spring Cloud** — for service discovery and configuration
- **API Gateway** — for routing and authentication

## Monorepo Structure

```
twitter/
├── 📁 build/                    # Build artifacts
├── 📁 compose/                  # Docker Compose configurations
│   └── postgre.yaml            # PostgreSQL configuration
├── 📁 docs/                     # Project documentation
├── 📁 logs/                     # Application logs
├── 📁 pictures/                 # Diagrams and images
│   └── architecture_1.png      # Architecture diagram
├── 📁 scripts/                  # Automation scripts
├── 📁 services/                 # Microservices
│   └── 📁 users-api/           # User management service
│       ├── 📁 src/              # Source code
│       ├── 📁 build/            # Service build artifacts
│       ├── Dockerfile           # Service Docker image
│       └── README.md            # Service documentation
├── 📁 shared/                   # Shared libraries
│   ├── 📁 common-lib/           # Common components
│   │   ├── 📁 src/              # Library source code
│   │   ├── build.gradle         # Build configuration
│   │   └── README.md            # Library documentation
│   └── 📁 database/             # Database components
│       ├── 📁 src/              # Source code
│       └── build.gradle         # Build configuration
├── 📁 sql/                      # SQL scripts
│   └── users.sql               # Users table schema
├── 📁 todo/                     # Development plans
│   ├── TODO_Gemini.md          # Gemini plans
│   ├── TODO_GPT5.md            # GPT-5 plans
│   ├── TODO_PATH.md            # Development roadmap
│   └── TODO_PLANS.md           # General plans
├── build.gradle                # Root Gradle configuration
├── docker-compose.yml          # Docker Compose for development
├── gradle.properties           # Gradle properties
├── settings.gradle             # Project settings
├── gradlew                     # Gradle Wrapper (Unix)
└── gradlew.bat                 # Gradle Wrapper (Windows)
```

### Module Descriptions

#### Services (`services/`)

- **`users-api`** — user management microservice
  - REST API for CRUD operations with users
  - Role-based model (USER, ADMIN, MODERATOR)
  - Secure password hashing
  - Data validation and filtering

#### Shared Libraries (`shared/`)

- **`common-lib`** — common components for all services
  - HTTP request logging (`@LoggableRequest`)
  - Global exception handling
  - Aspect-oriented programming (AOP)
  - Specialized exceptions

- **`database`** — database operation components
  - Common JPA entities
  - Repositories and specifications
  - Database connection configurations

## Build and Deployment

### Requirements

- **Java 24** — primary development language
- **Docker** — for containerization
- **Docker Compose** — for local development
- **Gradle** — build system (Gradle Wrapper included)

### Project Build

#### Building All Modules
```bash
# Build entire project
./gradlew build

# Build without tests (faster)
./gradlew build -x test

# Build specific service
./gradlew :services:users-api:build
```

#### Dependency Management

**Updating Dependency Versions:**
```bash
# Update version in root build.gradle
# Change version in dependencyManagement block

# Check dependencies
./gradlew dependencies

# Update dependencies
./gradlew --refresh-dependencies build
```

**Adding New Dependencies:**
1. Add version to `dependencyManagement` in root `build.gradle`
2. Add dependency without version to required subproject
3. Run `./gradlew build` to verify

**Example of Adding Dependencies:**
```gradle
// In root build.gradle (dependencyManagement)
dependencyManagement {
    dependencies {
        dependency 'com.example:new-library:1.2.3'
    }
}

// In subproject (without version)
dependencies {
    implementation 'com.example:new-library'
}
```

#### Building Docker Images
```bash
# Build users-api image
docker build -f services/users-api/Dockerfile -t twitter-users-api .

# Build all images via Docker Compose
docker-compose build

# Build and deploy everything at once (recommended)
docker-compose up -d --build
```

### Local Deployment

#### Option 1: Docker Compose (recommended)
```bash
# Start entire infrastructure
docker-compose up -d

# View logs
docker-compose logs -f users-api

# Stop
docker-compose down
```

#### Option 2: Local Development
```bash
# 1. Start PostgreSQL
docker-compose up -d postgres

# 2. Start service
./gradlew :services:users-api:bootRun

# Or via IDE
# Run Application.java in services/users-api/src/main/java/com/twitter/
```

## Deployment and Environments

### CI/CD Pipeline (planned)

#### Main Deployment Steps

1. **Build and Testing**
   ```bash
   ./gradlew clean build
   ./gradlew test
   ```

2. **Create Docker Image**
   ```bash
   docker build -f services/users-api/Dockerfile -t twitter-users-api:latest .
   ```

## Links

### Service Documentation

- **[Users API](services/users-api/README_eng.md)** — user management service documentation
- **[Common Library](shared/common-lib/README_eng.md)** — common library documentation

### External Resources

- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **PostgreSQL Documentation**: https://www.postgresql.org/docs/
- **Docker Documentation**: https://docs.docker.com/
- **Gradle Documentation**: https://docs.gradle.org/

### Architectural Decisions

- **ADR (Architecture Decision Records)** — planned creation of `docs/adr/` folder
- **Wiki** — planned integration with corporate Wiki
- **Confluence** — planned integration with Confluence

---

## Quick Start

1. **Clone Repository**
   ```bash
   git clone <repository-url>
   cd twitter
   ```

2. **Start Infrastructure**
   ```bash
   docker-compose up -d --build
   ```

3. **Verify Operation**
   ```bash
   curl http://localhost:8081/actuator/health
   ```

4. **API Documentation**
   - Swagger UI: `http://localhost:8081/swagger-ui.html` (planned)
   - OpenAPI Spec: `http://localhost:8081/v3/api-docs` (planned)

---

*Last updated: $(date)*
*Document version: 1.0*
