# Liquibase Standards for Twitter Microservices Project

## Overview

This document defines Liquibase standards and best practices for database migrations in the Twitter microservices project. These standards are based on the analysis of existing Liquibase changelog files in `admin-script-api` service and should be followed when creating or modifying database migrations.

**Key Principles:**
- All database schema changes MUST be managed through Liquibase migrations
- Never modify existing migrations after they have been applied to production
- Use sequential numbering for migration files
- Follow consistent naming conventions for tables, columns, and constraints
- Always include proper constraints (primary keys, foreign keys, unique constraints, check constraints)
- Use UUID for all primary keys
- Include audit fields (created_at, updated_at, is_deleted) where appropriate

**Technology Stack:**
- Liquibase 4.24
- PostgreSQL 15
- Spring Boot 3.5.5
- Java 24

---

## 1. File Structure

### 1.1 Directory Organization

**Standard directory structure for Liquibase changelogs:**

```
src/main/resources/
└── db/
    └── changelog/
        ├── db.changelog-master.xml          # Master changelog (includes all changes)
        └── changes/
            ├── 001-create-users-table.xml
            ├── 002-create-tweets-table.xml
            ├── 003-create-follows-table.xml
            ├── 004-create-tweet-likes-table.xml
            └── 005-create-tweet-retweets-table.xml
```

**Key points:**
- Master changelog file: `db.changelog-master.xml` in `db/changelog/` directory
- Individual migration files: in `db/changelog/changes/` subdirectory
- Migration files use sequential numbering: `001-`, `002-`, `003-`, etc.

### 1.2 Master Changelog File

**Structure of `db.changelog-master.xml`:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.24.xsd">

    <!-- Include all change sets in order -->
    <include file="db/changelog/changes/001-create-users-table.xml"/>
    <include file="db/changelog/changes/002-create-tweets-table.xml"/>
    <include file="db/changelog/changes/003-create-follows-table.xml"/>
    <include file="db/changelog/changes/004-create-tweet-likes-table.xml"/>
    <include file="db/changelog/changes/005-create-tweet-retweets-table.xml"/>

</databaseChangeLog>
```

**Requirements:**
- Use XML format with proper namespace declarations
- Include all migration files in sequential order
- Use relative paths: `db/changelog/changes/XXX-description.xml`
- Add comments for clarity when needed

### 1.3 Individual Migration Files

**Structure of individual migration file:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.24.xsd">

    <changeSet id="XXX-description" author="geron">
        <!-- Migration content -->
    </changeSet>

</databaseChangeLog>
```

**Requirements:**
- Each file contains one or more `changeSet` elements
- File name format: `XXX-description.xml` (e.g., `001-create-users-table.xml`)
- File name should match the changeSet id

---

## 2. Naming Conventions

### 2.1 ChangeSet IDs

**Format:** `XXX-description`

**Rules:**
- Use three-digit sequential number: `001`, `002`, `003`, etc.
- Use lowercase with hyphens for description
- Description should clearly indicate what the migration does
- ChangeSet ID must be unique across all migrations

**Examples:**
- `001-create-users-table`
- `002-create-tweets-table`
- `010-add-user-profile-column`
- `015-create-index-on-user-email`

### 2.2 Table Names

**Format:** `snake_case` (lowercase with underscores)

**Rules:**
- Use plural form for table names (e.g., `users`, `tweets`, `follows`)
- Use descriptive names that clearly indicate the table's purpose
- Keep names concise but meaningful

**Examples:**
- `users`
- `tweets`
- `tweet_likes`
- `tweet_retweets`
- `follows`

### 2.3 Column Names

**Format:** `snake_case` (lowercase with underscores)

**Rules:**
- Use descriptive names that clearly indicate the column's purpose
- For foreign key columns, use format: `{referenced_table}_id` (e.g., `user_id`, `tweet_id`)
- For audit columns, use standard names: `created_at`, `updated_at`, `is_deleted`, `deleted_at`

**Examples:**
- `id` - primary key
- `user_id` - foreign key to users table
- `tweet_id` - foreign key to tweets table
- `follower_id` - foreign key to users table (follower)
- `following_id` - foreign key to users table (following)
- `created_at` - creation timestamp
- `updated_at` - update timestamp
- `is_deleted` - soft delete flag
- `deleted_at` - deletion timestamp

### 2.4 Constraint Names

#### Primary Key Constraints

**Format:** Implicit (handled by Liquibase `primaryKey="true"`)

**Note:** Primary key constraints are automatically named by PostgreSQL, but the column definition should explicitly set `primaryKey="true"`.

#### Foreign Key Constraints

**Format:** `fk_{table}_{column}` or `{table}_{column}_fk`

**Rules:**
- Use descriptive names that indicate the relationship
- Include both table and column names
- Keep names concise

**Examples:**
- `fk_tweets_user_id`
- `tweet_likes_tweet_fk`
- `tweet_likes_user_fk`
- `follows_follower_fk`
- `follows_following_fk`

#### Unique Constraints

**Format:** `uk_{table}_{columns}` or `{table}_unique_{name}`

**Rules:**
- Use `uk_` prefix for unique constraints
- Include table name and column names
- For composite unique constraints, list all columns

**Examples:**
- `uk_tweet_likes_tweet_user` - composite unique on tweet_id and user_id
- `users_login_unique` - unique on login column
- `users_email_unique` - unique on email column
- `follows_unique_follower_following` - composite unique on follower_id and following_id

#### Check Constraints

**Format:** `chk_{table}_{condition}`

**Rules:**
- Use `chk_` prefix for check constraints
- Include table name and brief condition description
- Keep condition descriptions concise

**Examples:**
- `chk_content_length` - checks content is not empty
- `chk_content_max_length` - checks content length <= 280
- `follows_check_no_self_follow` - checks follower_id != following_id

---

## 3. ChangeSet Structure

### 3.1 Required Attributes

**Every changeSet MUST have:**
- `id` - unique identifier (format: `XXX-description`)
- `author` - author name (e.g., `geron`)

**Example:**
```xml
<changeSet id="001-create-users-table" author="geron">
    <!-- Migration content -->
</changeSet>
```

### 3.2 ChangeSet Content Order

**Recommended order for changeSet content:**

1. **Table creation** (`createTable`)
2. **Primary key** (defined in column with `primaryKey="true"`)
3. **Unique constraints** (`addUniqueConstraint`)
4. **Foreign key constraints** (`addForeignKeyConstraint`)
5. **Check constraints** (`sql` with ALTER TABLE)
6. **Indexes** (if needed)
7. **Data migrations** (if needed)

**Example:**
```xml
<changeSet id="002-create-tweets-table" author="geron">
    <!-- 1. Create table -->
    <createTable tableName="tweets">
        <!-- Columns with primary key -->
    </createTable>

    <!-- 2. Add foreign key constraints -->
    <addForeignKeyConstraint .../>

    <!-- 3. Add check constraints -->
    <sql>ALTER TABLE tweets ADD CONSTRAINT ...</sql>
</changeSet>
```

---

## 4. Data Types

### 4.1 Primary Keys

**ALWAYS use UUID for primary keys:**

```xml
<column name="id" type="uuid">
    <constraints primaryKey="true" nullable="false"/>
</column>
```

**Key points:**
- Type: `uuid`
- Always set `primaryKey="true"`
- Always set `nullable="false"`
- UUIDs are generated by the application, not by the database

### 4.2 Foreign Keys

**Use UUID for foreign key columns:**

```xml
<column name="user_id" type="uuid">
    <constraints nullable="false"/>
</column>
```

**Key points:**
- Type: `uuid`
- Always set `nullable="false"` (unless explicitly allowing NULL)
- Foreign key constraint is added separately using `addForeignKeyConstraint`

### 4.3 String Types

**Use VARCHAR with appropriate length:**

```xml
<!-- Short strings -->
<column name="login" type="varchar(50)">
    <constraints nullable="false"/>
</column>

<!-- Medium strings -->
<column name="content" type="varchar(280)">
    <constraints nullable="false"/>
</column>

<!-- Long strings -->
<column name="email" type="varchar(255)">
    <constraints nullable="false"/>
</column>
```

**Common lengths:**
- `varchar(20)` - status, role enums
- `varchar(50)` - short names, logins
- `varchar(255)` - emails, longer text
- `varchar(280)` - tweet content

### 4.4 Numeric Types

**Use INTEGER for counts and numeric values:**

```xml
<column name="likes_count" type="integer" defaultValueNumeric="0">
    <constraints nullable="false"/>
</column>

<column name="retweets_count" type="integer" defaultValueNumeric="0">
    <constraints nullable="false"/>
</column>
```

**Key points:**
- Use `integer` for counts and numeric values
- Use `defaultValueNumeric` for default numeric values
- Always set `nullable="false"` for counts

### 4.5 Boolean Types

**Use BOOLEAN for flags:**

```xml
<column name="is_deleted" type="boolean" defaultValueBoolean="false">
    <constraints nullable="false"/>
</column>
```

**Key points:**
- Type: `boolean`
- Use `defaultValueBoolean` for default boolean values
- Always set `nullable="false"` for boolean flags

### 4.6 Timestamp Types

**Use TIMESTAMP for date/time fields:**

```xml
<column name="created_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP">
    <constraints nullable="false"/>
</column>

<column name="updated_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP">
    <constraints nullable="false"/>
</column>

<column name="deleted_at" type="timestamp"/>
```

**Key points:**
- Type: `timestamp`
- Use `defaultValueComputed="CURRENT_TIMESTAMP"` for automatic timestamp
- `created_at` and `updated_at` should always be `nullable="false"`
- `deleted_at` can be nullable (only set when record is deleted)

---

## 5. Constraints

### 5.1 Primary Key Constraints

**Define primary key in column definition:**

```xml
<column name="id" type="uuid">
    <constraints primaryKey="true" nullable="false"/>
</column>
```

**Requirements:**
- Always use UUID for primary keys
- Always set `primaryKey="true"`
- Always set `nullable="false"`

### 5.2 Foreign Key Constraints

**Add foreign key constraints after table creation:**

```xml
<addForeignKeyConstraint
    baseTableName="tweets"
    baseColumnNames="user_id"
    constraintName="fk_tweets_user_id"
    referencedTableName="users"
    referencedColumnNames="id"/>
```

**Requirements:**
- Use `addForeignKeyConstraint` tag
- Specify `baseTableName` and `baseColumnNames`
- Specify `referencedTableName` and `referencedColumnNames`
- Use descriptive `constraintName` following naming conventions
- Always reference UUID primary keys

**Example with multiple foreign keys:**

```xml
<addForeignKeyConstraint
    baseTableName="tweet_likes"
    baseColumnNames="tweet_id"
    constraintName="tweet_likes_tweet_fk"
    referencedTableName="tweets"
    referencedColumnNames="id"/>

<addForeignKeyConstraint
    baseTableName="tweet_likes"
    baseColumnNames="user_id"
    constraintName="tweet_likes_user_fk"
    referencedTableName="users"
    referencedColumnNames="id"/>
```

### 5.3 Unique Constraints

**Add unique constraints after table creation:**

```xml
<!-- Single column unique constraint -->
<addUniqueConstraint
    tableName="users"
    columnNames="login"
    constraintName="users_login_unique"/>

<!-- Composite unique constraint -->
<addUniqueConstraint
    tableName="tweet_likes"
    columnNames="tweet_id, user_id"
    constraintName="uk_tweet_likes_tweet_user"/>
```

**Requirements:**
- Use `addUniqueConstraint` tag
- For composite constraints, separate column names with comma and space: `"column1, column2"`
- Use descriptive `constraintName` following naming conventions

**Common use cases:**
- Unique login/email in users table
- Composite unique for junction tables (e.g., one like per user per tweet)

### 5.4 Check Constraints

**Add check constraints using SQL:**

```xml
<sql>
    ALTER TABLE tweets ADD CONSTRAINT chk_content_length CHECK (LENGTH(TRIM(content)) &gt; 0);
</sql>
<sql>
    ALTER TABLE tweets ADD CONSTRAINT chk_content_max_length CHECK (LENGTH(content) &lt;= 280);
</sql>
```

**Requirements:**
- Use `<sql>` tag for check constraints
- Escape special characters: `&gt;` for `>`, `&lt;` for `<`
- Use descriptive constraint names following naming conventions
- Place check constraints after table creation

**Example with self-reference check:**

```xml
<sql>
    ALTER TABLE follows ADD CONSTRAINT follows_check_no_self_follow CHECK (follower_id != following_id);
</sql>
```

---

## 6. Audit Fields

### 6.1 Standard Audit Fields

**Standard audit fields for all tables:**

- `created_at` - timestamp when record was created
- `updated_at` - timestamp when record was last updated
- `is_deleted` - boolean flag for soft delete
- `deleted_at` - timestamp when record was deleted (nullable)

### 6.2 Created At Field

**Always include `created_at` field:**

```xml
<column name="created_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP">
    <constraints nullable="false"/>
</column>
```

**Requirements:**
- Type: `timestamp`
- Default: `CURRENT_TIMESTAMP`
- Always `nullable="false"`
- Should be set automatically by database

### 6.3 Updated At Field

**Include `updated_at` for tables that can be modified:**

```xml
<column name="updated_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP">
    <constraints nullable="false"/>
</column>
```

**Requirements:**
- Type: `timestamp`
- Default: `CURRENT_TIMESTAMP`
- Always `nullable="false"`
- Should be updated by application on record modification

### 6.4 Soft Delete Fields

**Include soft delete fields for tables that support soft delete:**

```xml
<column name="is_deleted" type="boolean" defaultValueBoolean="false">
    <constraints nullable="false"/>
</column>
<column name="deleted_at" type="timestamp"/>
```

**Requirements:**
- `is_deleted`: boolean, default `false`, not nullable
- `deleted_at`: timestamp, nullable (only set when deleted)
- Use soft delete for important data that should be recoverable

**Example table with all audit fields:**

```xml
<createTable tableName="tweets">
    <column name="id" type="uuid">
        <constraints primaryKey="true" nullable="false"/>
    </column>
    <column name="user_id" type="uuid">
        <constraints nullable="false"/>
    </column>
    <column name="content" type="varchar(280)">
        <constraints nullable="false"/>
    </column>
    <column name="created_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP">
        <constraints nullable="false"/>
    </column>
    <column name="updated_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP">
        <constraints nullable="false"/>
    </column>
    <column name="is_deleted" type="boolean" defaultValueBoolean="false">
        <constraints nullable="false"/>
    </column>
    <column name="deleted_at" type="timestamp"/>
    <column name="likes_count" type="integer" defaultValueNumeric="0">
        <constraints nullable="false"/>
    </column>
    <column name="retweets_count" type="integer" defaultValueNumeric="0">
        <constraints nullable="false"/>
    </column>
</createTable>
```

---

## 7. Creating New Migrations

### 7.1 Migration File Naming

**Steps to create a new migration:**

1. **Determine the next sequential number** - check existing migrations in `changes/` directory
2. **Create descriptive name** - use format: `XXX-description.xml`
3. **Create file** in `db/changelog/changes/` directory

**Example:**
- Last migration: `005-create-tweet-retweets-table.xml`
- Next migration: `006-add-user-profile-table.xml`

### 7.2 Adding to Master Changelog

**Add include statement to `db.changelog-master.xml`:**

```xml
<include file="db/changelog/changes/006-add-user-profile-table.xml"/>
```

**Important:**
- Add include statement in sequential order
- Place at the end of the include list
- Maintain proper XML formatting

### 7.3 ChangeSet Structure

**Create changeSet with proper structure:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.24.xsd">

    <changeSet id="006-add-user-profile-table" author="geron">
        <!-- Migration content -->
    </changeSet>

</databaseChangeLog>
```

### 7.4 Migration Types

#### Creating a New Table

**Example:**
```xml
<changeSet id="006-add-user-profile-table" author="geron">
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
```

#### Adding a Column

**Example:**
```xml
<changeSet id="007-add-user-avatar-column" author="geron">
    <addColumn tableName="users">
        <column name="avatar_url" type="varchar(500)"/>
    </addColumn>
</changeSet>
```

#### Modifying a Column

**Example:**
```xml
<changeSet id="008-modify-user-email-length" author="geron">
    <modifyDataType tableName="users" columnName="email" newDataType="varchar(320)"/>
</changeSet>
```

#### Creating an Index

**Example:**
```xml
<changeSet id="009-create-index-on-tweet-user-id" author="geron">
    <createIndex indexName="idx_tweets_user_id" tableName="tweets">
        <column name="user_id"/>
    </createIndex>
</changeSet>
```

---

## 8. Configuration

### 8.1 Gradle Configuration

**Add Liquibase dependency to `build.gradle`:**

```gradle
dependencies {
    // ... other dependencies
    
    implementation 'org.liquibase:liquibase-core'
    
    runtimeOnly 'org.postgresql:postgresql'
}
```

**Key points:**
- Use `implementation` for Liquibase core
- Use `runtimeOnly` for PostgreSQL driver
- No version needed (managed by Spring Boot BOM)

### 8.2 Application Configuration

**Configure Liquibase in `application.yml`:**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/twitter
    username: user
    password: password
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: none
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    enabled: true
    drop-first: false
```

**Configuration parameters:**
- `change-log` - path to master changelog file (relative to classpath)
- `enabled: true` - enable Liquibase migrations on application startup
- `drop-first: false` - do not drop existing tables before applying migrations

**Important:**
- **ALWAYS set `ddl-auto: none`** - Hibernate should not manage schema
- **ALWAYS set `drop-first: false`** in production environments
- Use `classpath:` prefix for changelog path

### 8.3 Docker Configuration

**For Docker environments, use `application-docker.yml`:**

```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    enabled: true
    drop-first: false
```

**Key points:**
- Same configuration as standard application.yml
- Migrations run automatically on container startup
- Database connection is configured separately

---

## 9. Best Practices

### 9.1 Migration Management

**DO:**
- ✅ Create new migrations for all schema changes
- ✅ Use sequential numbering for migration files
- ✅ Test migrations on test database before production
- ✅ Keep migrations small and focused (one logical change per migration)
- ✅ Use descriptive changeSet IDs and file names
- ✅ Include proper constraints (foreign keys, unique constraints, check constraints)
- ✅ Document complex migrations with comments

**DON'T:**
- ❌ Never modify existing migrations after they've been applied to production
- ❌ Never skip migration numbers
- ❌ Never create migrations that depend on application code (data migrations are OK)
- ❌ Never use `drop-first: true` in production
- ❌ Never set `ddl-auto: update` or `create` when using Liquibase

### 9.2 Naming and Organization

**DO:**
- ✅ Use consistent naming conventions (snake_case for tables/columns)
- ✅ Use descriptive names that clearly indicate purpose
- ✅ Group related changes in the same changeSet
- ✅ Keep changeSet IDs and file names synchronized

**DON'T:**
- ❌ Don't use camelCase or PascalCase for database objects
- ❌ Don't use abbreviations unless they're widely understood
- ❌ Don't create migrations with generic names like `update-table`

### 9.3 Constraints and Data Integrity

**DO:**
- ✅ Always define primary keys
- ✅ Always add foreign key constraints for relationships
- ✅ Add unique constraints for business rules (e.g., one like per user per tweet)
- ✅ Add check constraints for data validation (e.g., content length, no self-follow)
- ✅ Use NOT NULL constraints for required fields

**DON'T:**
- ❌ Don't skip foreign key constraints (they ensure data integrity)
- ❌ Don't allow NULL in primary keys or foreign keys (unless explicitly needed)
- ❌ Don't forget to add constraints for composite unique requirements

### 9.4 Performance Considerations

**DO:**
- ✅ Create indexes for frequently queried columns (foreign keys, search fields)
- ✅ Use appropriate data types (don't use VARCHAR(255) for everything)
- ✅ Consider partitioning for large tables (future migrations)

**DON'T:**
- ❌ Don't create unnecessary indexes (they slow down inserts/updates)
- ❌ Don't use TEXT for short strings (use VARCHAR with appropriate length)

### 9.5 Testing Migrations

**Before applying to production:**
1. Test on local database
2. Test on staging/test environment
3. Verify rollback procedures (if needed)
4. Check migration execution time
5. Verify data integrity after migration

---

## 10. Complete Examples

### 10.1 Simple Table Creation

**Example: Users table**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.24.xsd">

    <changeSet id="001-create-users-table" author="geron">
        <createTable tableName="users">
            <column name="id" type="uuid">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="login" type="varchar(50)">
                <constraints nullable="false"/>
            </column>
            <column name="first_name" type="varchar(50)"/>
            <column name="last_name" type="varchar(50)"/>
            <column name="email" type="varchar(255)">
                <constraints nullable="false"/>
            </column>
            <column name="password_hash" type="varchar(255)">
                <constraints nullable="false"/>
            </column>
            <column name="password_salt" type="varchar(255)">
                <constraints nullable="false"/>
            </column>
            <column name="status" type="varchar(20)">
                <constraints nullable="false"/>
            </column>
            <column name="role" type="varchar(20)">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>

        <addUniqueConstraint tableName="users" columnNames="login" constraintName="users_login_unique"/>
        <addUniqueConstraint tableName="users" columnNames="email" constraintName="users_email_unique"/>
    </changeSet>

</databaseChangeLog>
```

### 10.2 Table with Foreign Keys and Check Constraints

**Example: Tweets table**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.24.xsd">

    <changeSet id="002-create-tweets-table" author="geron">
        <createTable tableName="tweets">
            <column name="id" type="uuid">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="user_id" type="uuid">
                <constraints nullable="false"/>
            </column>
            <column name="content" type="varchar(280)">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="is_deleted" type="boolean" defaultValueBoolean="false">
                <constraints nullable="false"/>
            </column>
            <column name="deleted_at" type="timestamp"/>
            <column name="likes_count" type="integer" defaultValueNumeric="0">
                <constraints nullable="false"/>
            </column>
            <column name="retweets_count" type="integer" defaultValueNumeric="0">
                <constraints nullable="false"/>
            </column>
        </createTable>

        <addForeignKeyConstraint
            baseTableName="tweets"
            baseColumnNames="user_id"
            constraintName="fk_tweets_user_id"
            referencedTableName="users"
            referencedColumnNames="id"/>

        <sql>
            ALTER TABLE tweets ADD CONSTRAINT chk_content_length CHECK (LENGTH(TRIM(content)) &gt; 0);
        </sql>
        <sql>
            ALTER TABLE tweets ADD CONSTRAINT chk_content_max_length CHECK (LENGTH(content) &lt;= 280);
        </sql>
    </changeSet>

</databaseChangeLog>
```

### 10.3 Junction Table with Composite Unique Constraint

**Example: Tweet likes table**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.24.xsd">

    <changeSet id="004-create-tweet-likes-table" author="geron">
        <createTable tableName="tweet_likes">
            <column name="id" type="uuid">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="tweet_id" type="uuid">
                <constraints nullable="false"/>
            </column>
            <column name="user_id" type="uuid">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>

        <addForeignKeyConstraint
            baseTableName="tweet_likes"
            baseColumnNames="tweet_id"
            constraintName="tweet_likes_tweet_fk"
            referencedTableName="tweets"
            referencedColumnNames="id"/>

        <addForeignKeyConstraint
            baseTableName="tweet_likes"
            baseColumnNames="user_id"
            constraintName="tweet_likes_user_fk"
            referencedTableName="users"
            referencedColumnNames="id"/>

        <addUniqueConstraint
            tableName="tweet_likes"
            columnNames="tweet_id, user_id"
            constraintName="uk_tweet_likes_tweet_user"/>
    </changeSet>

</databaseChangeLog>
```

### 10.4 Table with Self-Reference Check Constraint

**Example: Follows table**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.24.xsd">

    <changeSet id="003-create-follows-table" author="geron">
        <createTable tableName="follows">
            <column name="id" type="uuid">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="follower_id" type="uuid">
                <constraints nullable="false"/>
            </column>
            <column name="following_id" type="uuid">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>

        <addForeignKeyConstraint
            baseTableName="follows"
            baseColumnNames="follower_id"
            constraintName="follows_follower_fk"
            referencedTableName="users"
            referencedColumnNames="id"/>

        <addForeignKeyConstraint
            baseTableName="follows"
            baseColumnNames="following_id"
            constraintName="follows_following_fk"
            referencedTableName="users"
            referencedColumnNames="id"/>

        <addUniqueConstraint
            tableName="follows"
            columnNames="follower_id, following_id"
            constraintName="follows_unique_follower_following"/>

        <sql>
            ALTER TABLE follows ADD CONSTRAINT follows_check_no_self_follow CHECK (follower_id != following_id);
        </sql>
    </changeSet>

</databaseChangeLog>
```

---

## 11. Troubleshooting

### 11.1 Common Issues

#### Migration Fails on Startup

**Problem:** Liquibase migration fails when application starts.

**Solutions:**
- Check database connection settings in `application.yml`
- Verify changelog file path is correct
- Check for syntax errors in XML files
- Ensure database user has necessary permissions
- Review Liquibase logs for specific error messages

#### Duplicate ChangeSet ID

**Problem:** Error about duplicate changeSet ID.

**Solutions:**
- Ensure each changeSet has a unique ID
- Check that file names match changeSet IDs
- Verify no duplicate includes in master changelog

#### Foreign Key Constraint Violation

**Problem:** Foreign key constraint fails during migration.

**Solutions:**
- Ensure referenced table exists before creating foreign key
- Check migration order in master changelog
- Verify referenced column exists and has correct type

#### Check Constraint Syntax Error

**Problem:** SQL syntax error in check constraint.

**Solutions:**
- Escape special characters: `&gt;` for `>`, `&lt;` for `<`
- Verify SQL syntax is valid PostgreSQL
- Test SQL statement directly in database before adding to migration

### 11.2 Migration Tracking

**Liquibase tracks applied migrations in `databasechangelog` table:**

- `id` - changeSet ID
- `author` - changeSet author
- `filename` - changelog file name
- `dateexecuted` - when migration was applied
- `md5sum` - checksum of changeSet content

**Important:**
- Never manually modify `databasechangelog` table
- Liquibase uses MD5 checksums to detect changes to existing migrations
- If you modify an existing migration, Liquibase will detect the change and may fail

---

## 12. Version History

- **v1.0** (2025-01-27): Initial version based on analysis of admin-script-api Liquibase migrations

---

## References

- [Liquibase Documentation](https://docs.liquibase.com/)
- [Liquibase XML Format](https://docs.liquibase.com/change-types/home.html)
- [PostgreSQL Data Types](https://www.postgresql.org/docs/current/datatype.html)
- [Spring Boot Liquibase Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.liquibase)