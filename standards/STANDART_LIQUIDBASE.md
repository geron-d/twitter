# Liquibase Standards for Twitter Microservices Project

## Overview

This document defines a single, non-duplicated standard for Liquibase migrations in the project.

**Key principles:**
- All schema changes are implemented only via Liquibase migrations
- Applied production migrations are immutable
- Migration files and `changeSet` IDs use sequential numbering
- Naming conventions for tables, columns, and constraints are mandatory
- Primary keys and foreign keys use UUID
- Constraints are explicit (PK, FK, unique, check)
- Audit fields are added where business logic requires them

**Technology stack:**
- Liquibase 4.24
- PostgreSQL 15
- Spring Boot 3.5.5
- Java 24

---

## 1. File Structure and Lifecycle

### 1.1 Standard Structure

```text
src/main/resources/
└── db/
    └── changelog/
        ├── db.changelog-master.xml
        └── changes/
            ├── 001-create-users-table.xml
            ├── 002-create-tweets-table.xml
            └── ...
```

### 1.2 Master Changelog Rules

- Master file path: `db/changelog/db.changelog-master.xml`
- Migration files path: `db/changelog/changes/`
- Include files in strict sequential order
- Include format: `db/changelog/changes/XXX-description.xml`

Example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.24.xsd">

    <include file="db/changelog/changes/001-create-users-table.xml"/>
    <include file="db/changelog/changes/002-create-tweets-table.xml"/>
</databaseChangeLog>
```

### 1.3 How to Add a New Migration

1. Check the latest file in `db/changelog/changes/`
2. Create next file: `XXX-description.xml`
3. Add `changeSet id="XXX-description" author="geron"`
4. Add new `<include .../>` to master changelog at the end

---

## 2. Naming Conventions

### 2.1 `changeSet` and File Naming

- File format: `XXX-description.xml`
- `changeSet` ID format: `XXX-description`
- `XXX` is a 3-digit sequential number (`001`, `002`, ...)
- Description uses lowercase and hyphens
- File name and `changeSet` ID must match
- `changeSet` ID must be unique globally

### 2.2 Database Object Naming

- Tables: `snake_case`, plural (`users`, `tweet_likes`)
- Columns: `snake_case`
- Foreign key columns: `{referenced_table}_id`
- Audit columns: `created_at`, `updated_at`, `is_deleted`, `deleted_at`

### 2.3 Constraint Naming

- Foreign keys: `fk_{table}_{column}` or `{table}_{column}_fk`
- Unique: `uk_{table}_{columns}` or `{table}_unique_{name}`
- Check: `chk_{table}_{condition}`

Keep names concise, descriptive, and consistent across migrations.

---

## 3. ChangeSet Standard

### 3.1 Required Attributes

Every `changeSet` must have:
- `id` in format `XXX-description`
- `author="geron"` (project default)

### 3.2 Recommended Internal Order

Inside a single `changeSet`, use this order when applicable:
1. `createTable`
2. Primary key declaration inside column constraints
3. `addUniqueConstraint`
4. `addForeignKeyConstraint`
5. Check constraints (`<sql>` with `ALTER TABLE`)
6. Indexes
7. Data migration steps

### 3.3 Immutability Rule

- Never modify a migration already applied in production
- Any change is implemented via a new migration file

---

## 4. Data Types and Column Rules

### 4.1 UUID Keys

Primary key:

```xml
<column name="id" type="uuid">
    <constraints primaryKey="true" nullable="false"/>
</column>
```

Foreign key:

```xml
<column name="user_id" type="uuid">
    <constraints nullable="false"/>
</column>
```

Rules:
- PK and FK types are `uuid`
- PK is always `primaryKey="true"` and `nullable="false"`
- FK is non-nullable by default unless business logic allows null

### 4.2 Common Types

- Short text: `varchar(50)`
- Email/long text: `varchar(255)`
- Content: `varchar(280)` when business rule requires it
- Counters: `integer` with `defaultValueNumeric="0"` and `nullable="false"`
- Flags: `boolean` with `defaultValueBoolean="false"` and `nullable="false"`
- Timestamps: `timestamp` (`created_at` and `updated_at` are non-null)

Timestamp pattern:

```xml
<column name="created_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP">
    <constraints nullable="false"/>
</column>
```

---

## 5. Constraints and Data Integrity

### 5.1 Foreign Key Constraint

```xml
<addForeignKeyConstraint
    baseTableName="tweets"
    baseColumnNames="user_id"
    constraintName="fk_tweets_user_id"
    referencedTableName="users"
    referencedColumnNames="id"/>
```

### 5.2 Unique Constraint

```xml
<addUniqueConstraint
    tableName="tweet_likes"
    columnNames="tweet_id, user_id"
    constraintName="uk_tweet_likes_tweet_user"/>
```

### 5.3 Check Constraint

```xml
<sql>
    ALTER TABLE tweets ADD CONSTRAINT chk_content_max_length CHECK (LENGTH(content) &lt;= 280);
</sql>
```

Rules:
- Use `addForeignKeyConstraint` for relations
- Use `addUniqueConstraint` for business uniqueness
- Use `<sql>` for checks and escape XML operators (`&lt;`, `&gt;`)
- Add constraints after table creation

---

## 6. Audit Fields Policy

Use audit fields according to table semantics:
- Mandatory for mutable business entities: `created_at`, `updated_at`
- Soft-delete support when required: `is_deleted`, `deleted_at`

Soft-delete pattern:

```xml
<column name="is_deleted" type="boolean" defaultValueBoolean="false">
    <constraints nullable="false"/>
</column>
<column name="deleted_at" type="timestamp"/>
```

Rules:
- `created_at`: non-null, default `CURRENT_TIMESTAMP`
- `updated_at`: non-null, default `CURRENT_TIMESTAMP`, updated by application
- `is_deleted`: non-null, default `false`
- `deleted_at`: nullable

---

## 7. Spring Configuration

### 7.1 Gradle

```gradle
dependencies {
    implementation 'org.liquibase:liquibase-core'
    runtimeOnly 'org.postgresql:postgresql'
}
```

### 7.2 Application YAML

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    enabled: true
    drop-first: false
```

Rules:
- `ddl-auto` must be `none` when Liquibase is active
- `drop-first` must be `false` for production
- Use `classpath:` for changelog path

---

## 8. Best Practices Checklist

**Do:**
- Create new migration for every schema change
- Keep migrations small and logically focused
- Test migrations locally and in staging
- Verify integrity and execution time before production
- Add indexes only for real query patterns

**Do not:**
- Modify already applied production migrations
- Skip sequence numbers
- Use `ddl-auto: update/create` with Liquibase
- Use `drop-first: true` in production
- Add unnecessary indexes

---

## 9. Minimal Complete Example

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.24.xsd">

    <changeSet id="010-create-user-profiles-table" author="geron">
        <createTable tableName="user_profiles">
            <column name="id" type="uuid">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="user_id" type="uuid">
                <constraints nullable="false"/>
            </column>
            <column name="bio" type="varchar(500)"/>
            <column name="created_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>

        <addForeignKeyConstraint
            baseTableName="user_profiles"
            baseColumnNames="user_id"
            constraintName="fk_user_profiles_user_id"
            referencedTableName="users"
            referencedColumnNames="id"/>

        <addUniqueConstraint
            tableName="user_profiles"
            columnNames="user_id"
            constraintName="uk_user_profiles_user_id"/>
    </changeSet>
</databaseChangeLog>
```

---

## Version History

- **v1.1** (2026-03-31): Removed duplicated rules and examples, consolidated standard sections
- **v1.0** (2025-01-27): Initial version based on admin-script-api Liquibase migrations

---

## References

- [Liquibase Documentation](https://docs.liquibase.com/)
- [Liquibase XML Format](https://docs.liquibase.com/change-types/home.html)
- [PostgreSQL Data Types](https://www.postgresql.org/docs/current/datatype.html)
- [Spring Boot Liquibase Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.liquibase)
