# Docker Standards for Java / Spring Boot Services

## Overview

This document defines Docker standards and best practices. These standards should be followed when creating or modifying
Docker-related files.

**Key Principles** (normative detail by area: **§1** Dockerfile, **§2** Compose, **§3** `.dockerignore`):

- Multi-stage builds; non-root user in runtime; health checks in image and Compose
- Layer caching: copy Gradle wrapper and build files before sources (see §1.3 / §1.9–§1.10)
- Concrete image tags (avoid `latest`); pin patch or digests in production (see §1.2)
- `.dockerignore` at context root; no secrets in images or compose files

**Technology Stack:**

- Java 24
- Spring Boot 3.5.5
- Gradle (**default: single-module root project**; **multi-module only when required** — see Gradle Layout below)
- PostgreSQL 15 and/or MongoDB 7 — **optional for local development**; enable only the databases you need via Docker Compose **profiles** (see §2.15)
- Docker Compose (Compose V2; `version` key optional)

---

## Gradle Layout: Single-Module (Default) vs Multi-Module

**Default — single-module (root project):**

- One deployable Spring Boot JAR built from the repository root (`settings.gradle` without extra `include`, or a single
  application subproject that is clearly the only image).
- Build task: `./gradlew bootJar` (with `-x test` and other flags as in templates).
- Runtime `COPY` path: `/app/build/libs/app.jar` (or the configured `bootJar` archive name — prefer a fixed name such as
  `app.jar` via `bootJar { archiveFileName.set("app.jar") }` for stable Docker `COPY`).

**Multi-module — only when needed:**

- Use when the repository ships **multiple independently deployable** applications, or when a shared library layout
  clearly requires separate Gradle subprojects and separate images.
- `settings.gradle` / `settings.gradle.kts` must `include` subprojects; Dockerfile build task targets the module:
  `./gradlew :[module-path]:bootJar` (or `:build` if not using Spring Boot plugin on that module).
- Runtime `COPY` path: `/app/[module-path]/build/libs/*.jar` (prefer a single known JAR name to avoid glob pitfalls).

**Do not** default to paths like `services/[service-name]/` unless your repository actually uses that layout (e.g.
monorepo with many APIs).

**Decision summary:**

| Situation                     | Layout             | Typical Gradle command              | Typical JAR path in image            |
|-------------------------------|--------------------|-------------------------------------|--------------------------------------|
| One API, one JAR              | Single-module root | `./gradlew bootJar`                 | `/app/build/libs/app.jar`            |
| Several APIs, separate images | Multi-module       | `./gradlew :services:api-a:bootJar` | `/app/services/api-a/build/libs/...` |

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

### 1.2 Base Image Tags and Pinning

- Prefer **concrete tags** over `latest` (already required above).
- For **production**, pin **patch-level** tags (e.g. `eclipse-temurin:24.0.1_9-jre`) or **image digests** (
  `FROM eclipse-temurin@sha256:...`) so builds are reproducible and updates are deliberate.
- Document any pinned digest in the Dockerfile comment when used.

### 1.3 Build Stage Requirements

**Base Image:** `gradle:jdk24` (or a pinned variant such as `gradle:8.14-jdk24`); never use `latest` as the only
reference (see §1.2).

**Working directory:** `WORKDIR /app` (see **§1.9** / **§1.10** build stage).

**Copy order (caching):** Gradle wrapper and build descriptors first, then full tree, then `chmod`, `./gradlew
dependencies`, then `./gradlew bootJar` (or `:module:bootJar`). **Authoritative copy-paste:** **§1.9** (single-module)
or **§1.10** (multi-module). JAR output paths vs Gradle Layout: see the table in **Gradle Layout** above; example
monorepo: `:services:users-api:bootJar` → JAR under `/app/services/users-api/build/libs/`.

### 1.4 Runtime Stage Requirements

**Base Image:** `eclipse-temurin:24-jre` (not JDK); pin patch or digest for production (§1.2).

**Security:** **MUST** create a non-root user and **MUST** run the process as that user (see runtime stage in **§1.9** /
**§1.10**). Multi-module JAR copy path: `COPY --from=build /app/[module-path]/build/libs/app.jar app.jar` (see **§1.10**).

### 1.5 Port Exposure

**Always** `EXPOSE [port-number]` (see **§1.9** / **§1.10**). Illustrative ports: API A 8081, API B 8082, admin API
8083, single app (e.g. music) 8080.

### 1.6 JVM Configuration

Set heap, G1, and container support via `ENV JAVA_OPTS=...` (full line in **§1.9** / **§1.10**). Typical flags:
`-Xms512m`, `-Xmx1024m`, `-XX:+UseG1GC`, `-XX:+UseContainerSupport`.

### 1.7 Health Checks

**MUST** include `HEALTHCHECK` using `curl` against `http://localhost:[port]/actuator/health`. Full directive and
parameter meanings (`interval`, `timeout`, `start-period`, `retries`): **§1.9** / **§1.10** runtime stage.

### 1.8 Entry Point

Use shell form so `JAVA_OPTS` expands: **§1.9** / **§1.10** `ENTRYPOINT`.

### 1.9 Complete Dockerfile Template A — Single-Module (Default)

```dockerfile
# Multi-stage build for [Application Name]
# Stage 1: Build stage with Gradle
FROM gradle:jdk24 AS build

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew gradlew.bat ./
COPY build.gradle settings.gradle ./
COPY gradle.properties ./

COPY . .

RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon

RUN ./gradlew bootJar -x test --no-daemon --parallel --build-cache

# Stage 2: Runtime stage
FROM eclipse-temurin:24-jre

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

COPY --from=build /app/build/libs/app.jar app.jar

RUN mkdir -p /app/logs && chown -R appuser:appuser /app

USER appuser

EXPOSE [port-number]

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:[port-number]/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 1.10 Complete Dockerfile Template B — Multi-Module

```dockerfile
# Multi-stage build for [Service Name] (Gradle subproject :[module-path])
FROM gradle:jdk24 AS build

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew gradlew.bat ./
COPY build.gradle settings.gradle ./
COPY gradle.properties ./

COPY . .

RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon

RUN ./gradlew :[module-path]:bootJar -x test --no-daemon --parallel --build-cache

FROM eclipse-temurin:24-jre

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

COPY --from=build /app/[module-path]/build/libs/app.jar app.jar

RUN mkdir -p /app/logs && chown -R appuser:appuser /app

USER appuser

EXPOSE [port-number]

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:[port-number]/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

Replace `[module-path]` with Gradle path segments without colons (e.g. `services/users-api` for `:services:users-api`).

---

## 2. Docker Compose Standards

### 2.1 Compose File Version

**Docker Compose V2** (Docker CLI plugin) does not require a top-level `version` key. For new files, **omit** `version`
unless you must target legacy Compose file format.

If maintaining older tooling that expects a schema version:

```yaml
version: '3.8'
```

Prefer documenting `version`-less compose as the default for new projects.

### 2.2 Service Naming

**Naming conventions:**

- Use kebab-case for service names (e.g. `users-api`, `music-api`)
- Use descriptive container names: `[project]-[service-name]` (e.g. `music-api`, `music-postgres`)
- Replace `[project]` with your product or repository short name consistently across networks and containers

### 2.3 Build Configuration

**Single-module — Dockerfile at repository root:**

```yaml
services:
    [ service-name ]:
        build:
            context: .
            dockerfile: Dockerfile
        container_name: [ project ]-[service-name]
```

**Multi-module — Dockerfile next to a subproject (still use root context when the build needs the whole monorepo):**

```yaml
services:
    [ service-name ]:
        build:
            context: .
            dockerfile: [ module-path ]/Dockerfile
        container_name: [ project ]-[service-name]
```

**Key points:**

- When the Gradle build needs the full tree, `context` should be the **repository root** (`.`)
- `dockerfile` is relative to the context directory
- Always specify `container_name` for easier identification when appropriate

### 2.4 Port Mapping

**Format: `"host-port:container-port"`:**

```yaml
ports:
    - "8080:8080"  # example single app
```

For multiple APIs, assign distinct host ports per service.

### 2.5 Environment Variables

**PostgreSQL + Spring JDBC (canonical map; reuse via YAML anchor `*spring-postgres-jdbc` in §2.13–§2.14):**

```yaml
environment: &spring-postgres-jdbc
    SPRING_PROFILES_ACTIVE: docker
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/[database-name]
    SPRING_DATASOURCE_USERNAME: user
    SPRING_DATASOURCE_PASSWORD: password
    SPRING_DATASOURCE_DRIVER_CLASS_NAME: org.postgresql.Driver
    SPRING_JPA_HIBERNATE_DDL_AUTO: validate
    SPRING_JPA_SHOW_SQL: false
    LOGGING_LEVEL_ROOT: INFO
```

Use a logging package key that matches your base package (e.g. `LOGGING_LEVEL_COM_EXAMPLE: DEBUG`). If you do not use YAML
anchors, copy this map under `environment:` for each service.

**Best practices:**

- Use `SPRING_PROFILES_ACTIVE: docker` for Docker environment
- Reference other services by service name (e.g., `postgres:5432`)
- Set appropriate logging levels
- Use `validate` for `ddl-auto` in production-like environments

**Spring Boot — MongoDB (when the application uses Spring Data MongoDB):**

```yaml
environment:
    SPRING_PROFILES_ACTIVE: docker
    SPRING_DATA_MONGODB_URI: mongodb://user:password@mongodb:27017/[database-name]?authSource=admin
```

Alternatively (property-style):

```yaml
environment:
    SPRING_DATA_MONGODB_HOST: mongodb
    SPRING_DATA_MONGODB_PORT: 27017
    SPRING_DATA_MONGODB_DATABASE: "[database-name]"
    SPRING_DATA_MONGODB_USERNAME: user
    SPRING_DATA_MONGODB_PASSWORD: password
    SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE: admin
```

If the repository defines **both** JDBC and MongoDB in the same service, configure only the stores you use and exclude unused auto-configuration in `application-docker.yml` (e.g. `spring.autoconfigure.exclude`) so the container does not require a database it does not use.

### 2.6 Service Dependencies

**Use `depends_on` with health conditions** when a dependent must wait until an upstream service is healthy (applications,
optional admin tools such as pgAdmin, etc.):

```yaml
depends_on:
    postgres:
        condition: service_healthy
```

**Dependency order:**

1. Database services first
2. Core application services
3. Dependent or auxiliary services

### 2.7 Networks

**Define custom network:**

```yaml
networks:
    [ project ]-network:
                   driver: bridge
                   ipam:
                       config:
                           -   subnet: 172.20.0.0/16
```

**Assign services to network:**

```yaml
services:
    service-name:
        networks:
            - [ project ]-network
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

**Define health checks for all long-running application and database services:**

```yaml
healthcheck:
    test: [ "CMD", "curl", "-f", "http://localhost:[port]/actuator/health" ]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 60s
```

**For database services:**

```yaml
healthcheck:
    test: [ "CMD-SHELL", "pg_isready -U user -d [database-name]" ]
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
    container_name: [ project ]-postgres
    restart: unless-stopped
    environment:
        POSTGRES_DB: [ database-name ]
        POSTGRES_USER: user
        POSTGRES_PASSWORD: password
        POSTGRES_INITDB_ARGS: "--encoding=UTF-8 --lc-collate=C --lc-ctype=C"
    ports:
        - "5432:5432"
    volumes:
        - postgres_data:/var/lib/postgresql/data
        - ./sql:/docker-entrypoint-initdb.d
    networks:
        - [ project ]-network
    healthcheck:
        test: [ "CMD-SHELL", "pg_isready -U user -d [database-name]" ]
        interval: 10s
        timeout: 5s
        retries: 5
    # Optional: when using Compose profiles (§2.15), add so Postgres is not started by default:
    # profiles:
    #     - postgres
```

**Profiles:** If PostgreSQL is optional locally, add `profiles: [ postgres ]` as above and align app services with the
same profile (§2.15). **`SPRING_DATASOURCE_URL`** in the application must still use hostname **`postgres`** on the
compose network.

### 2.12 Optional Admin UI (e.g. pgAdmin)

Pin the image tag (avoid `latest`). Wait for Postgres to be healthy before starting:

```yaml
pgadmin:
    image: dpage/pgadmin4:9
    profiles:
        - tools
    container_name: [ project ]-pgadmin
    restart: unless-stopped
    environment:
        PGADMIN_DEFAULT_EMAIL: admin@example.dev
        PGADMIN_DEFAULT_PASSWORD: password
    ports:
        - "5050:80"
    depends_on:
        postgres:
            condition: service_healthy
    networks:
        - [ project ]-network
```

### 2.13 Application Service Template (Root Dockerfile)

**`environment`:** `*spring-postgres-jdbc` requires anchor **`&spring-postgres-jdbc`** in the same file (§2.5); otherwise
paste the map from §2.5. **`depends_on`:** §2.6. **`networks` / `volumes` / `restart`:** §2.7, §2.8, §2.10. **`healthcheck`:**
add `interval`, `timeout`, `retries`, and `start_period` exactly as in §2.9 (application services).

```yaml
services:
    [ service-name ]:
        build:
            context: .
            dockerfile: Dockerfile
        container_name: [ project ]-[service-name]
        ports:
            - "[port]:[port]"
        environment: *spring-postgres-jdbc
        depends_on:
            postgres:
                condition: service_healthy
        networks:
            - [ project ]-network
        volumes:
            - ./logs:/app/logs
        restart: unless-stopped
        healthcheck:
            test: [ "CMD", "curl", "-f", "http://localhost:[port]/actuator/health" ]
            # interval, timeout, retries, start_period: §2.9
```

### 2.14 Application Service Template (Monorepo Subproject Dockerfile)

Same as §2.13 except **`build.dockerfile`:**

```yaml
services:
    [ service-name ]:
        build:
            context: .
            dockerfile: [ module-path ]/Dockerfile
        container_name: [ project ]-[service-name]
        ports:
            - "[port]:[port]"
        environment: *spring-postgres-jdbc
        depends_on:
            postgres:
                condition: service_healthy
        networks:
            - [ project ]-network
        volumes:
            - ./logs:/app/logs
        restart: unless-stopped
        healthcheck:
            test: [ "CMD", "curl", "-f", "http://localhost:[port]/actuator/health" ]
            # interval, timeout, retries, start_period: §2.9
```

### 2.15 Optional Databases and Tools (Compose Profiles)

Use **Compose profiles** so PostgreSQL, MongoDB, and admin UIs start **only when needed**, without duplicating entire compose files.

**Assign profiles to optional services:**

- `postgres` — PostgreSQL
- `mongo` — MongoDB
- `tools` — optional tooling (e.g. pgAdmin)

Example (illustrative):

```yaml
services:
    postgres:
        profiles:
            - postgres
        # ... image, environment, healthcheck, volumes
    mongodb:
        profiles:
            - mongo
        # ... see §2.16
    pgadmin:
        profiles:
            - tools
        depends_on:
            postgres:
                condition: service_healthy
    music-api:
        profiles:
            - postgres
        depends_on:
            postgres:
                condition: service_healthy
```

**Application service (`depends_on`):**

- Declare `depends_on` **only for databases the application actually uses**. If a dependency is disabled by profile, Compose does not require it for that run (the app may use an external database on the host).
- When both databases exist in the file but the app uses only PostgreSQL, do **not** add `depends_on: mongodb` for that application.
- **Compose validation:** If the database service uses a profile (e.g. `postgres`), assign the **same profile** to the Spring Boot service that `depends_on` it. Otherwise some Compose versions report an invalid project (`depends on undefined service`). When no profile is active, those services are omitted and `docker compose up` may start nothing; use `--profile` as documented below.

**Typical commands (Compose V2):**

- Application only (e.g. external DB or JAR outside Compose): run the app without the compose profile that bundles the DB, or use a dedicated profile/overrides documented in the repo
- Application + PostgreSQL: `docker compose --profile postgres up`
- Add MongoDB: `docker compose --profile postgres --profile mongo up`
- Add pgAdmin (requires Postgres healthy): `docker compose --profile postgres --profile tools up`

**Host port conflicts:** If PostgreSQL (`5432`) or MongoDB (`27017`) are already bound on the host, change the **left** side of `ports` (e.g. `"5433:5432"`) or omit publishing and connect only from other containers on the compose network.

Document the chosen profiles and commands in the repository README **Docker** section so developers do not forget `--profile`.

### 2.16 MongoDB Service Template

Pin the image tag (e.g. `mongo:7`). Use a named volume and a health check compatible with the image (MongoDB 6+ ships `mongosh`).

```yaml
mongodb:
    image: mongo:7
    container_name: [ project ]-mongodb
    restart: unless-stopped
    profiles:
        - mongo
    environment:
        MONGO_INITDB_ROOT_USERNAME: user
        MONGO_INITDB_ROOT_PASSWORD: password
        MONGO_INITDB_DATABASE: [ database-name ]
    ports:
        - "27017:27017"
    volumes:
        - mongo_data:/data/db
    networks:
        - [ project ]-network
    healthcheck:
        test: [ "CMD", "mongosh", "--quiet", "--eval", "db.runCommand({ ping: 1 }).ok" ]
        interval: 10s
        timeout: 5s
        retries: 5
        start_period: 30s
```

Declare `mongo_data` under top-level `volumes` (same pattern as `postgres_data`).

For applications that use a **non-root** user instead of root credentials, prefer init scripts or a custom entrypoint; keep secrets out of Git and use `env_file` or Docker secrets in production.

---

## 3. .dockerignore Standards

### 3.1 Purpose

A **`.dockerignore` file MUST exist** at the **root of the Docker build context** (typically the repository root when
`build.context` is `.`). Docker only reads `.dockerignore` from the context root, not from the Dockerfile’s directory
unless that directory is also the context.

Excluding unnecessary paths reduces build context size, speeds up `docker build`, and avoids leaking secrets into unused
layers.

### 3.2 Critical: Do Not Exclude Gradle Wrapper or Wrapper Directory

**NEVER** list the following in `.dockerignore` when the Dockerfile runs `./gradlew` inside the build stage:

- `gradle/` (wrapper JAR and properties live here)
- `gradlew`
- `gradlew.bat`

Excluding them **breaks** the build stage.

### 3.3 Exclusion Categories

Use **§3.5** as the **only** full pattern list to copy into `.dockerignore`. Below: **why** each category exists (no
duplicate globs).

- **Gradle outputs and cache** — shrink context; **never** exclude `gradle/`, `gradlew`, or `gradlew.bat` (§3.2)
- **IDE / editor** — local metadata not needed in the image
- **OS-generated files** — noise and platform-specific cruft
- **Logs and temporary paths** — avoid stale logs and scratch files in context
- **Tests** — optional omission of `src/test/` (and similar) on very large trees
- **Git** — history not needed for `docker build`
- **Compose and local Docker files** — usually not required inside the image layer
- **Documentation and tooling** — optional; often exclude `*.md` with `!README.md` if the image does not need docs
- **Environment and secrets** — keep `.env*` out of context when possible
- **Node artifacts** — if the repository contains JavaScript tooling

Whether to exclude `Dockerfile` and `.dockerignore` from context is optional; excluding them does not affect
`docker build` instruction parsing (the client sends those separately). Many teams exclude them for slightly smaller
context listings.

### 3.4 Optional: Narrow Context (Advanced)

For very large monorepos, teams may use a **smaller build context** or BuildKit-only patterns. That is **not** the
default standard; document any such setup per repository. Default remains **root context** + root `.dockerignore` as
above.

### 3.5 Complete .dockerignore Template (Root Context, Gradle Build Inside Image)

Authoritative list for copy-paste; **§3.3** explains categories without repeating every pattern.

```dockerignore
# Gradle — outputs and cache only (NOT gradle/ nor gradlew)
build/
.gradle/

# IDE
.idea/
*.iml
*.iws
*.ipr
.vscode/
*.swp
*.swo

# OS
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

# Tests
src/test/
**/test/
**/tests/

# Git
.git/
.gitignore
.gitattributes

# Local env and secrets
.env
.env.local
.env.*.local

# Docs and tooling (optional)
*.md
!README.md
docs/
.cursor/
.plans/

# Compose (optional)
docker-compose*.yml

# Node (if any)
node_modules/
package*.json
yarn.lock
```

---

## 4. Best Practices

Cross-cutting rules already stated in **Overview** and **§1–§3** are not repeated here. This section adds **operational and supply-chain** guidance.

### 4.1 Security

- **Never commit secrets** in Dockerfiles or `docker-compose.yml`; use `env_file` or secrets in production
- **Tag every image** in Compose (databases, tools such as pgAdmin, not only app images); pin patch or digest in production (see Overview and §1.2)
- **Keep base images updated**; **scan images** for vulnerabilities before deployment
- **Prefer minimal images** (e.g. Alpine-based) where compatible with your stack

### 4.2 Performance

- **Copy order and multi-stage builds**: see §1.3 and §1.9–§1.10
- **Build context size**: see §3
- **Combine `RUN`** commands where it reduces layers without hurting readability; **remove** unused packages after install in the same layer

### 4.3 Maintainability

- **Add comments** to Dockerfiles explaining non-obvious choices
- **Use descriptive service names** in `docker-compose.yml`
- **Document environment variables** in `docker-compose.yml`
- **Version control** all Docker-related files
- **Keep Dockerfiles simple** — avoid complex logic

### 4.4 Monitoring

- **Semantics of health checks** (Dockerfile `HEALTHCHECK`, Compose `healthcheck`): §1.7, §2.9
- **Operational**: tune `interval` / `start_period` for slower starts in production; **monitor logs** via volume mounts (§2.8); **use restart policies** as in §2.10

### 4.5 Resource Management

- **Set appropriate JVM memory limits** based on container resources
- **Use resource limits** in docker-compose.yml for production-like runs. Example (Compose file format v3; **deploy** is fully applied in Swarm; with Docker Engine in non-Swarm mode, behavior depends on the client — many teams still document desired limits here for parity with orchestrated environments):

```yaml
services:
    music-api:
        deploy:
            resources:
                limits:
                    cpus: "1.0"
                    memory: 1G
                reservations:
                    cpus: "0.5"
                    memory: 512M
```

For local Docker Desktop without Swarm, you may use `mem_limit` / `cpus` at the service level where supported, or rely on JVM `JAVA_OPTS` and host constraints; pick one approach per environment and document it.

- **Monitor resource usage** and adjust accordingly
- **JVM heap and G1**: see §1.6 and **§1.9** / **§1.10** `JAVA_OPTS`

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

Mount `./sql` (or your scripts dir) to **`/docker-entrypoint-initdb.d`** on the Postgres service — see **§2.11** (`volumes`).
PostgreSQL runs scripts in **alphabetical** order.

### 5.3 Log Management

Bind-mount host **`./logs`** to **`/app/logs`** — see **§2.8** and application templates **§2.13** / **§2.14**. Ensure the
application writes logs under **`/app/logs`** (matches Dockerfile in **§1.9** / **§1.10**).

### 5.4 Development vs Production

**Spring:**

```yaml
environment:
    SPRING_PROFILES_ACTIVE: docker
```

**Compose:**

- Use **Compose profiles** (see §2.15) to toggle PostgreSQL, MongoDB, and tools without maintaining multiple near-duplicate files.
- Optionally maintain separate files for different environments:
  - `docker-compose.yml` — default local stack
  - `docker-compose.prod.yml` — production overrides (or a separate directory)

Combine approaches when useful: profiles for optional databases in `docker-compose.yml`, and `docker compose -f docker-compose.yml -f docker-compose.prod.yml` for production overrides.

---

## 6. Troubleshooting

### 6.1 Build Failures

**Common issues:**

- Missing dependencies in build stage
- Incorrect Gradle module path (multi-module)
- Permission issues with gradlew
- `.dockerignore` accidentally excludes `gradlew` or `gradle/`

**Solutions:**

- Verify Gradle wrapper is executable
- Check module path in build command
- Ensure all required files are copied
- Review `.dockerignore` against §3.2

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

- Increase `start_period` / tune intervals (semantics: **§1.7**, **§2.9**)
- Verify actuator endpoint is enabled
- Check network configuration (**§2.7**)

---

## 7. Version History

- **v1.3** (2026-03-29): Documentation deduplication: **Overview** vs **§4**; **§1.3–§1.8** point to **§1.9–§1.10**;
  **§2.5** canonical JDBC `environment` with YAML anchor `&spring-postgres-jdbc`; **§2.6** duplicate `depends_on` example
  removed; **§2.11** optional `profiles` for Postgres (supersedes former **§2.17**); **§2.13–§2.14** use anchor and
  shortened `healthcheck` vs **§2.9**; **§3.3** categories only, **§3.5** full template (OS globs aligned); **§5.2–§5.3** and
  **§6.3** cross-reference **§2** / **§1**; **§4.5** JVM pointer to **§1.6**; former **§2.17** merged into **§2.11**
- **v1.2** (2026-03-29): Optional PostgreSQL/MongoDB via Compose **profiles** (§2.15); MongoDB service template and health
  check (§2.16); Spring Data MongoDB environment variables (§2.5); `profiles` note for PostgreSQL (§2.17); pgAdmin example
  with `profiles: [tools]`; require **tagged images** for all compose services in §4.1; resource limits example and
  non-Swarm caveat in §4.5; §5.4 extended with profiles vs multi-file compose; Technology Stack clarifies databases are
  optional
- **v1.1** (2026-03-28): Default single-module Gradle layout; multi-module when required; dual Dockerfile templates;
  fixed `.dockerignore` (never exclude wrapper); context-root placement for `.dockerignore`; Compose V2 `version` note;
  neutral `[project]` naming; optional pgAdmin with `service_healthy`; base image pinning guidance
- **v1.0** (2025-01-27): Initial version based on analysis of existing Docker configurations

---

## References

- [Dockerfile Best Practices](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Project Standards](./STANDART_PROJECT.md)
- [Code Standards](./STANDART_CODE.md)
