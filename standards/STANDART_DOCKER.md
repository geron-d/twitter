# Docker Standards for Twitter Microservices Project

## Overview

This document defines Docker standards and best practices for the Twitter microservices project. These standards are based on the analysis of existing Docker configurations (Dockerfile, docker-compose.yml, .dockerignore) and should be followed when creating or modifying Docker-related files.

**Key Principles:**
- Use multi-stage builds for optimized image size
- Always run containers as non-root users
- Implement health checks for all services
- Optimize layer caching for faster builds
- Use specific image tags (avoid `latest`)
- Follow security best practices

**Technology Stack:**
- Java 24
- Spring Boot 3.5.5
- Gradle (Multi-module project)
- PostgreSQL 15
- Docker Compose 3.8

---

## 1. Dockerfile Standards

### 1.1 Multi-Stage Build Structure

**All Dockerfiles MUST use multi-stage builds** with at least two stages:
1. **Build stage** - for compiling and building the application
2. **Runtime stage** - for running the application

**Required structure:**
```dockerfile
# Multi-stage build for [Service Name]
# Stage 1: Build stage with Gradle
FROM gradle:jdk24 AS build

# Stage 2: Runtime stage
FROM eclipse-temurin:24-jre
```

### 1.2 Build Stage Requirements

**Base Image:**
- Use `gradle:jdk24` for build stage
- Never use `latest` tag - always specify version

**Working Directory:**
```dockerfile
WORKDIR /app
```

**File Copying Order (for optimal caching):**
1. Copy Gradle wrapper and configuration files first
2. Download dependencies (creates cacheable layer)
3. Copy source code
4. Build application

**Example:**
```dockerfile
# Copy gradle files first for better caching
COPY gradle/ gradle/
COPY gradlew gradlew.bat ./
COPY build.gradle settings.gradle ./
COPY gradle.properties ./

# Copy the entire project structure
COPY . .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies first (this layer will be cached)
RUN ./gradlew dependencies --no-daemon

# Build the application with optimized flags
RUN ./gradlew :services:[service-name]:build -x test --no-daemon --parallel --build-cache
```

### 1.3 Runtime Stage Requirements

**Base Image:**
- Use `eclipse-temurin:24-jre` (not JDK - smaller size)
- Always specify version tag

**Security Requirements:**
- **MUST create and use non-root user**
- **MUST switch to non-root user before running application**

**Example:**
```dockerfile
# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/services/[service-name]/build/libs/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser
```

### 1.4 Port Exposure

**Always explicitly expose ports:**
```dockerfile
EXPOSE [port-number]
```

**Port mapping:**
- users-api: 8081
- tweet-api: 8082
- admin-script-api: 8083
- follower-api: 8084

### 1.5 JVM Configuration

**Set JVM options via environment variable:**
```dockerfile
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"
```

**Key JVM options:**
- `-Xms512m` - Initial heap size
- `-Xmx1024m` - Maximum heap size
- `-XX:+UseG1GC` - Use G1 garbage collector
- `-XX:+UseContainerSupport` - Enable container-aware memory limits

### 1.6 Health Checks

**All Dockerfiles MUST include HEALTHCHECK:**
```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:[port]/actuator/health || exit 1
```

**Health check parameters:**
- `--interval=30s` - Check every 30 seconds
- `--timeout=3s` - Timeout after 3 seconds
- `--start-period=60s` - Allow 60 seconds for startup
- `--retries=3` - Mark unhealthy after 3 consecutive failures

### 1.7 Entry Point

**Use shell form for ENTRYPOINT to support environment variables:**
```dockerfile
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 1.8 Complete Dockerfile Template

```dockerfile
# Multi-stage build for Twitter [Service Name] API
# Stage 1: Build stage with Gradle
FROM gradle:jdk24 AS build

# Set working directory
WORKDIR /app

# Copy gradle files first for better caching
COPY gradle/ gradle/
COPY gradlew gradlew.bat ./
COPY build.gradle settings.gradle ./
COPY gradle.properties ./

# Copy the entire project structure
COPY . .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies first (this layer will be cached)
RUN ./gradlew dependencies --no-daemon

# Build the application with optimized flags
RUN ./gradlew :services:[service-name]:build -x test --no-daemon --parallel --build-cache

# Stage 2: Runtime stage
FROM eclipse-temurin:24-jre

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/services/[service-name]/build/libs/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE [port-number]

# Set JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:[port-number]/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

---

## 2. Docker Compose Standards

### 2.1 Version and Structure

**Use Docker Compose version 3.8:**
```yaml
version: '3.8'
```

### 2.2 Service Naming

**Naming conventions:**
- Use kebab-case for service names (e.g., `users-api`, `tweet-api`)
- Use descriptive container names: `twitter-[service-name]`
- Example: `twitter-users-api`, `twitter-tweet-api`

### 2.3 Build Configuration

**For services built from source:**
```yaml
services:
  service-name:
    build:
      context: .
      dockerfile: services/[service-name]/Dockerfile
    container_name: twitter-[service-name]
```

**Key points:**
- Context should be project root (`.`)
- Dockerfile path relative to context
- Always specify container_name for easier identification

### 2.4 Port Mapping

**Format: `"host-port:container-port"`:**
```yaml
ports:
  - "8081:8081"  # users-api
  - "8082:8082"  # tweet-api
  - "8083:8083"  # admin-script-api
  - "8084:8084"  # follower-api
```

### 2.5 Environment Variables

**Use environment variables for configuration:**
```yaml
environment:
  SPRING_PROFILES_ACTIVE: docker
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/twitter
  SPRING_DATASOURCE_USERNAME: user
  SPRING_DATASOURCE_PASSWORD: password
  SPRING_DATASOURCE_DRIVER_CLASS_NAME: org.postgresql.Driver
  SPRING_JPA_HIBERNATE_DDL_AUTO: validate
  SPRING_JPA_SHOW_SQL: false
  LOGGING_LEVEL_COM_TWITTER: DEBUG
```

**Best practices:**
- Use `SPRING_PROFILES_ACTIVE: docker` for Docker environment
- Reference other services by service name (e.g., `postgres:5432`)
- Set appropriate logging levels
- Use `validate` for `ddl-auto` in production

### 2.6 Service Dependencies

**Use `depends_on` with health conditions:**
```yaml
depends_on:
  postgres:
    condition: service_healthy
  users-api:
    condition: service_healthy
```

**Dependency order:**
1. Database services first
2. Core services (users-api)
3. Dependent services (tweet-api, follower-api)
4. Admin services last (admin-script-api)

### 2.7 Networks

**Define custom network:**
```yaml
networks:
  twitter-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

**Assign services to network:**
```yaml
services:
  service-name:
    networks:
      - twitter-network
```

### 2.8 Volumes

**For persistent data:**
```yaml
volumes:
  postgres_data:
    driver: local
```

**For logs:**
```yaml
services:
  service-name:
    volumes:
      - ./logs:/app/logs
```

### 2.9 Health Checks in Docker Compose

**Define health checks for all services:**
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:[port]/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

**For database services:**
```yaml
healthcheck:
  test: [ "CMD-SHELL", "pg_isready -U user -d twitter" ]
  interval: 10s
  timeout: 5s
  retries: 5
```

### 2.10 Restart Policies

**Use `unless-stopped` for production:**
```yaml
restart: unless-stopped
```

**Options:**
- `no` - Do not restart
- `always` - Always restart
- `on-failure` - Restart on failure
- `unless-stopped` - Restart unless explicitly stopped (recommended)

### 2.11 Database Service Template

```yaml
postgres:
  image: postgres:15-alpine
  container_name: twitter-postgres
  restart: unless-stopped
  environment:
    POSTGRES_DB: twitter
    POSTGRES_USER: user
    POSTGRES_PASSWORD: password
    POSTGRES_INITDB_ARGS: "--encoding=UTF-8 --lc-collate=C --lc-ctype=C"
  ports:
    - "5432:5432"
  volumes:
    - postgres_data:/var/lib/postgresql/data
    - ./sql:/docker-entrypoint-initdb.d
  networks:
    - twitter-network
  healthcheck:
    test: [ "CMD-SHELL", "pg_isready -U user -d twitter" ]
    interval: 10s
    timeout: 5s
    retries: 5
```

### 2.12 Application Service Template

```yaml
service-name:
  build:
    context: .
    dockerfile: services/[service-name]/Dockerfile
  container_name: twitter-[service-name]
  ports:
    - "[port]:[port]"
  environment:
    SPRING_PROFILES_ACTIVE: docker
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/twitter
    SPRING_DATASOURCE_USERNAME: user
    SPRING_DATASOURCE_PASSWORD: password
    SPRING_DATASOURCE_DRIVER_CLASS_NAME: org.postgresql.Driver
    SPRING_JPA_HIBERNATE_DDL_AUTO: validate
    SPRING_JPA_SHOW_SQL: false
    LOGGING_LEVEL_COM_TWITTER: DEBUG
    LOGGING_LEVEL_ORG_HIBERNATE_SQL: DEBUG
  depends_on:
    postgres:
      condition: service_healthy
  networks:
    - twitter-network
  volumes:
    - ./logs:/app/logs
  restart: unless-stopped
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:[port]/actuator/health"]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 60s
```

---

## 3. .dockerignore Standards

### 3.1 Purpose

**.dockerignore file MUST be created** in each service directory to exclude unnecessary files from Docker build context, reducing build time and image size.

### 3.2 Required Exclusions

**Gradle build files:**
```
build/
.gradle/
gradle/
gradlew
gradlew.bat
```

**IDE files:**
```
.idea/
*.iml
.vscode/
*.swp
*.swo
```

**OS generated files:**
```
.DS_Store
.DS_Store?
._*
.Spotlight-V100
.Trashes
ehthumbs.db
Thumbs.db
```

**Logs:**
```
*.log
logs/
```

**Temporary files:**
```
*.tmp
*.temp
tmp/
```

**Test files:**
```
src/test/
```

**Git files:**
```
.git/
.gitignore
```

**Docker files:**
```
Dockerfile
.dockerignore
```

**Documentation:**
```
*.md
docs/
```

**Environment files:**
```
.env
.env.local
.env.*.local
```

### 3.3 Complete .dockerignore Template

```dockerignore
# Gradle build files
build/
.gradle/
gradle/
gradlew
gradlew.bat

# IDE files
.idea/
*.iml
.vscode/
*.swp
*.swo

# OS generated files
.DS_Store
.DS_Store?
._*
.Spotlight-V100
.Trashes
ehthumbs.db
Thumbs.db

# Logs
*.log
logs/

# Temporary files
*.tmp
*.temp
tmp/

# Test files
src/test/

# Git
.git/
.gitignore

# Docker
Dockerfile
.dockerignore

# Documentation
*.md
docs/

# Configuration files that shouldn't be in container
.env
.env.local
.env.*.local

# Node modules (if any)
node_modules/

# Package files
package*.json
yarn.lock
```

---

## 4. Best Practices

### 4.1 Security

- **Always use non-root user** in containers
- **Never commit secrets** in Dockerfiles or docker-compose.yml
- **Use specific image tags** (avoid `latest`)
- **Keep base images updated** regularly
- **Scan images** for vulnerabilities before deployment
- **Use minimal base images** (alpine variants when possible)

### 4.2 Performance

- **Optimize layer caching** by copying files in order of change frequency
- **Use multi-stage builds** to reduce final image size
- **Remove unnecessary packages** after installation
- **Use .dockerignore** to exclude files from build context
- **Combine RUN commands** to reduce layers (when appropriate)

### 4.3 Maintainability

- **Add comments** to Dockerfiles explaining non-obvious choices
- **Use descriptive service names** in docker-compose.yml
- **Document environment variables** in docker-compose.yml
- **Version control** all Docker-related files
- **Keep Dockerfiles simple** - avoid complex logic

### 4.4 Monitoring

- **Implement health checks** for all services
- **Use health check conditions** in depends_on
- **Configure appropriate intervals** for health checks
- **Monitor container logs** via volumes
- **Use restart policies** appropriately

### 4.5 Resource Management

- **Set appropriate JVM memory limits** based on container resources
- **Use resource limits** in docker-compose.yml for production
- **Monitor resource usage** and adjust accordingly
- **Use G1GC** for better container memory management

---

## 5. Common Patterns

### 5.1 Inter-Service Communication

**Use service names for communication:**
```yaml
environment:
  USERS_API_URL: http://users-api:8081
```

**In application code, reference by service name:**
```java
@FeignClient(name = "users-api", url = "${USERS_API_URL}")
```

### 5.2 Database Initialization

**Mount SQL scripts:**
```yaml
volumes:
  - ./sql:/docker-entrypoint-initdb.d
```

**PostgreSQL will execute scripts in alphabetical order**

### 5.3 Log Management

**Mount logs directory:**
```yaml
volumes:
  - ./logs:/app/logs
```

**Ensure application writes to `/app/logs` directory**

### 5.4 Development vs Production

**Use profiles:**
```yaml
environment:
  SPRING_PROFILES_ACTIVE: docker
```

**Create separate docker-compose files:**
- `docker-compose.yml` - Development
- `docker-compose.prod.yml` - Production

---

## 6. Troubleshooting

### 6.1 Build Failures

**Common issues:**
- Missing dependencies in build stage
- Incorrect Gradle module path
- Permission issues with gradlew

**Solutions:**
- Verify Gradle wrapper is executable
- Check module path in build command
- Ensure all required files are copied

### 6.2 Runtime Failures

**Common issues:**
- Database connection failures
- Missing environment variables
- Port conflicts

**Solutions:**
- Check service dependencies in docker-compose.yml
- Verify environment variables are set
- Ensure ports are not already in use

### 6.3 Health Check Failures

**Common issues:**
- Application not ready
- Incorrect health check endpoint
- Network connectivity issues

**Solutions:**
- Increase `start_period` if needed
- Verify actuator endpoint is enabled
- Check network configuration

---

## 7. Version History

- **v1.0** (2025-01-27): Initial version based on analysis of existing Docker configurations

---

## References

- [Dockerfile Best Practices](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Project Standards](./STANDART_PROJECT.md)
- [Code Standards](./STANDART_CODE.md)
