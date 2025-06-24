# Upsert Repository

[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.mpecan/upsert)](https://central.sonatype.com/artifact/io.github.mpecan/upsert)

A Spring Data JPA extension that provides upsert capabilities for repositories. This library simplifies the process of inserting or updating records in a database using Spring Data JPA.

## Full disclosure

This project was built with the help of the following AI tools:

- [JetBrains Junie](https://www.jetbrains.com/junie/)
- [GitHub Copilot](https://copilot.github.com/)
- [Claude-Code](https://docs.anthropic.com/en/docs/agents-and-tools/claude-code/overview)

That said: all of the code has been reviewed and refinished by me, and I have made sure that it is
all correct and functional.

## Features

- Upsert a single entity or a list of entities
- Support for custom ON clauses and ignored fields
- Conditional upserts with comparison operators (>, >=, <, <=)
- Compatible with Spring Data JPA repositories
- Database-specific optimizations for MySQL and PostgreSQL
- Automatic handling of generated keys
- Batch operation support for improved performance

## What is Upsert?

"Upsert" is a combination of "update" and "insert" - it's an operation that will:
- Insert a new record if it doesn't exist
- Update an existing record if it does exist

This is particularly useful when you don't know whether a record exists and want to ensure it's created or updated in a single operation.

It also generally performs better than separate insert and update operations, especially when
dealing with large datasets. For data based comparisons please see
the [Performance Testing](PERFORMANCE-TESTING.md) document and
the [Performance Report](PERFORMANCE-REPORT.md).

## Using the Library

This library is available on Maven Central. You can add it to your project using:

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.mpecan:upsert:1.5.0")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'io.github.mpecan:upsert:1.5.0'
}
```

### Maven

```xml

<dependency>
  <groupId>io.github.mpecan</groupId>
  <artifactId>upsert</artifactId>
  <version>1.5.0</version>
</dependency>
```

## Database Support

This library supports the following databases:

- **PostgreSQL**: Uses the `INSERT ... ON CONFLICT ... DO UPDATE` syntax (requires PostgreSQL 9.5+)
- **MySQL**: Uses the `INSERT ... ON DUPLICATE KEY UPDATE` syntax

For detailed information about each implementation, see:
- [MySQL Implementation Details](docs/mysql.md)
- [PostgreSQL Implementation Details](docs/postgresql.md)

## Usage

### Basic Usage

To use the upsert capabilities, your repository interface should extend `UpsertRepository`:

```kotlin
interface UserRepository : UpsertRepository<User, Long> {
    // Standard Spring Data JPA methods
    fun findByUsername(username: String): User?
}
```

Then you can use the `upsert` and `upsertAll` methods:

```kotlin
// Upsert a single entity
val user = User(username = "john", email = "john@example.com")
userRepository.upsert(user)

// Upsert multiple entities
val users = listOf(
    User(username = "john", email = "john@example.com"),
    User(username = "jane", email = "jane@example.com")
)
userRepository.upsertAll(users)
```

### Custom ON Clauses and Ignored Fields

You can also use custom ON clauses and ignored fields by defining methods in your repository interface with specific naming patterns:

```kotlin
interface UserRepository : UpsertRepository<User, Long> {
    // Upsert using username as the ON clause
    fun upsertOnUsername(user: User): Int

    // Upsert using username as the ON clause and ignoring updatedAt field
    fun upsertOnUsernameIgnoringUpdatedAt(user: User): Int

    // Upsert all using username as the ON clause
    fun upsertAllOnUsername(users: List<User>): Int

    // Upsert all using username as the ON clause and ignoring updatedAt field
    fun upsertAllOnUsernameIgnoringUpdatedAt(users: List<User>): Int

    // Upsert using username and email as the ON clause
    fun upsertOnUsernameAndEmail(user: User): Int

    // Upsert using username and email as the ON clause and ignoring all fields
    // This will only insert new rows and not update existing ones
    fun upsertOnUsernameAndEmailIgnoringAllFields(user: User): Int
}
```

The method name is parsed to extract the following information:
- `upsert` or `upsertAll`: Whether to upsert a single entity or a list of entities
- `On<FieldName>`: The field(s) to use for the ON clause (e.g., `OnUsername`, `OnUsernameAndEmail`)
- `Ignoring<FieldName>`: The field(s) to ignore during updates (e.g., `IgnoringUpdatedAt`)
- `IgnoringAllFields`: Whether to ignore all fields during updates (only insert new rows)

### Conditional Upserts

Since version 1.3.0, the library supports conditional upserts using the `When` clause in method names. This allows you to specify conditions under which the update should occur, preventing updates when certain conditions are not met.

You can use comparison operators to check field values:
- `More` (>): Update only when the new value is greater than the existing value
- `MoreOrEqual` (>=): Update only when the new value is greater than or equal to the existing value
- `Less` (<): Update only when the new value is less than the existing value
- `LessOrEqual` (<=): Update only when the new value is less than or equal to the existing value

```kotlin
interface UserRepository : UpsertRepository<User, Long> {
    // Update only if the new updatedAt is more recent than the existing one
    fun upsertOnIdWhenUpdatedAtMore(user: User): Int
    
    // Update only if the new version is greater than or equal to the existing one
    fun upsertOnIdWhenVersionMoreOrEqual(user: User): Int
    
    // Update only if the new price is less than the existing one
    fun upsertOnIdWhenPriceLess(user: User): Int
    
    // Combine conditional with ignored fields
    fun upsertOnIdWhenVersionMoreIgnoringCreatedAt(user: User): Int
    
    // Batch operations with conditions
    fun upsertAllOnIdWhenUpdatedAtMore(users: List<User>): Int
}
```

This is particularly useful for:
- **Optimistic locking**: Update only if the version number is higher
- **Time-based updates**: Update only with more recent data
- **Price protection**: Prevent accidental price increases
- **Concurrent update protection**: Avoid overwriting newer data with older data

## Configuration

The library is automatically configured when you include it in your Spring Boot application. No
additional configuration is required.

Simply add the dependency to your project and create repositories that extend `UpsertRepository`:

```kotlin
@SpringBootApplication
@EnableJpaRepositories(
    basePackages = ["com.example.repositories"]
)
class Application {
    // ...
}
```

### Legacy Configuration (Pre-1.1.0)

In older versions, you needed to explicitly specify the `UpsertRepositoryFactoryBean`:

```kotlin
@Configuration
@EnableJpaRepositories(
    repositoryFactoryBeanClass = UpsertRepositoryFactoryBean::class
)
class AppConfig {
    // ...
}
```

This is no longer necessary as the library now uses Spring Boot's auto-configuration mechanism.

# Type Mapping System

The type mapping system provides a centralized, extensible way to handle Java/Kotlin to SQL type
conversions in the upsert library.

## Overview

The system consists of:

1. `TypeMapper` interface - Defines how Java/Kotlin types are mapped to SQL types
2. `TypeMapperRegistry` - Central registry for type mappers
3. `DefaultTypeMapper` - Handles common types

## Usage

### Default Behavior

The library comes with support for common Java/Kotlin types out of the box. You don't need to do
anything to use these mappings.

### Registering Custom Type Mappers

To add support for custom types, create a custom TypeMapper implementation and register it as a
Spring bean:

```kotlin
@Component
class MyCustomTypeMapper : TypeMapper {
    override fun canHandle(field: Field): Boolean {
        return field.type == MyJsonType::class.java
    }

    override fun canHandleValue(value: Any?): Boolean {
        return value is MyJsonType
    }

    override fun convertToJdbcValue(value: Any?): Any? {
        if (value is MyJsonType) {
            return objectMapper.writeValueAsString(value)
        }
        return value
    }
}
```

### Creating a Custom Type Mapper for a Library

If you're creating a library that extends the upsert library with additional type support:

```kotlin
// In your library's auto-configuration class
@Configuration
class MyLibraryConfiguration {
    @Bean
    fun myCustomTypeMapper(): TypeMapper {
        return object : TypeMapper {
            override fun canHandle(field: Field): Boolean {
                return field.type == MyCustomType::class.java
            }

            override fun canHandleValue(value: Any?): Boolean {
                return value is MyCustomType
            }

            override fun convertToJdbcValue(value: Any?): Any? {
                if (value is MyCustomType) {
                    // Convert MyCustomType to a JDBC-compatible value
                    return convertMyType(value)
                }
                return value
            }
        }
    }
}
```

# JSON Mapping in Upsert Library

This document explains how to use the JSON mapping capabilities in your application.

## Including the Dependencies

To use the JSON mapping capabilities, you need to include at least one JSON library in your project.

### Gradle

Add one of the following dependencies to your `build.gradle` or `build.gradle.kts`:

```kotlin
// Option 1: Jackson (preferred)
implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2") // If using Kotlin

// Option 2: Gson
implementation("com.google.code.gson:gson:2.10.1")

// Option 3: JSON-B
implementation("jakarta.json.bind:jakarta.json.bind-api:3.0.0")
implementation("org.eclipse:yasson:3.0.3") // JSON-B implementation
```

### Maven

Add one of the following dependencies to your `pom.xml`:

```xml
<!-- Option 1: Jackson (preferred) -->
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-databind</artifactId>
  <version>1.5.0</version>
</dependency>
<dependency>
  <groupId>com.fasterxml.jackson.module</groupId>
  <artifactId>jackson-module-kotlin</artifactId>
  <version>1.5.0</version>
</dependency>

<!-- Option 2: Gson -->
<dependency>
  <groupId>com.google.code.gson</groupId>
  <artifactId>gson</artifactId>
  <version>1.5.0</version>
</dependency>

<!-- Option 3: JSON-B -->
<dependency>
  <groupId>jakarta.json.bind</groupId>
  <artifactId>jakarta.json.bind-api</artifactId>
  <version>1.5.0</version>
</dependency>
<dependency>
  <groupId>org.eclipse</groupId>
  <artifactId>yasson</artifactId>
  <version>1.5.0</version>
</dependency>
```

## Using JSON Mapping

Once you've included a JSON library, the library will automatically configure the appropriate JSON
mapper. You can then use JSON mapping in your entity classes:

```kotlin
@Entity
@Table(name = "product")
data class Product(
    @Id
    val id: Long,

    // Option 1: Explicit JSON column definition
    @Column(columnDefinition = "jsonb") // or "json"
    val attributes: Map<String, String>,

    // Option 2: Automatic detection of common JSON types
    val tags: List<String>,

    // Option 3: Custom classes
    val metadata: ProductMetadata
)

data class ProductMetadata(
    val manufacturer: String,
    val countryOfOrigin: String
)
```

## Testing

For testing, it's recommended to include Jackson in your test dependencies:

```kotlin
// Gradle
testImplementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
```

```xml
<!-- Maven -->
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-databind</artifactId>
  <version>1.5.0</version>
  <scope>test</scope>
</dependency>
<dependency>
<groupId>com.fasterxml.jackson.module</groupId>
<artifactId>jackson-module-kotlin</artifactId>
<version>1.5.0</version>
<scope>test</scope>
</dependency>
```

## Library Priority

The library automatically selects a JSON mapper in the following order:

1. Jackson
2. Gson
3. JSON-B

If multiple libraries are present, the highest priority one will be used.

## Custom JSON Mappers

If you need custom JSON serialization, you can provide your own `JsonTypeMapper` implementation:

```kotlin
@Component
@Primary // To override the default mapper
class MyCustomJsonTypeMapper : AbstractJsonTypeMapper() {
    override fun toJson(value: Any): String {
        // Your custom JSON serialization logic here
        return "..."
    }
}
```

## How It Works

When the library needs to determine a SQL type for a field or value:

1. It asks the `TypeMapperRegistry` for the appropriate type mapper
2. The registry checks all registered mappers to find one that can handle the type
3. The mapper determines the SQL type and any necessary value conversion

For JPA-annotated fields with `@Convert` annotations, the system:

1. Detects the converter class
2. Determines the target SQL type based on the converter's output type
3. Uses the converter to transform values when needed

## Extension Points

You can extend the type mapping system in several ways:

1. Create a custom `TypeMapper` implementation and register it as a Spring bean
2. Override the default mapper by creating a bean with higher precedence
3. Create a library with auto-configuration that provides additional type mappers

## Best Practices

1. Use Spring's dependency injection to register type mappers
2. Implement the `TypeMapper` interface for your custom types
3. Use the `@Order` annotation to control the precedence of your type mappers
4. Test your type mappers with a variety of input values

## Implementation Details

### How It Works

1. **Method Parsing**: When you call an upsert method, the library parses the method name to determine the operation type, ON clause fields, and ignored fields.
2. **SQL Generation**: The library generates the appropriate SQL statement based on the database type and the parsed method information.
3. **Execution**: The SQL statement is executed using Spring's `JdbcTemplate`.
4. **Generated Keys**: Any generated keys (such as auto-increment IDs) are retrieved and set on the entity objects.

### Database-Specific Implementations

#### MySQL

MySQL uses the `INSERT ... ON DUPLICATE KEY UPDATE` syntax for upsert operations. This relies on the presence of a unique or primary key constraint on the table.

Example:
```sql
INSERT INTO users (id, username, email)
VALUES (:id, :username, :email)
ON DUPLICATE KEY UPDATE
    username = VALUES(username),
    email = VALUES(email)
```

With conditional updates (MySQL 8.0.19+):
```sql
INSERT INTO users (id, username, email, version)
VALUES (:id, :username, :email, :version)
ON DUPLICATE KEY UPDATE
    username = IF(VALUES(version) > version, VALUES(username), username),
    email = IF(VALUES(version) > version, VALUES(email), email),
    version = IF(VALUES(version) > version, VALUES(version), version)
```

[Learn more about MySQL implementation](docs/mysql.md)

#### PostgreSQL

PostgreSQL uses the `INSERT ... ON CONFLICT ... DO UPDATE` syntax for upsert operations. This allows for more control over which columns are used for conflict detection.

Example:
```sql
INSERT INTO users (id, username, email)
VALUES (:id, :username, :email)
ON CONFLICT (id) DO UPDATE SET
    username = EXCLUDED.username,
    email = EXCLUDED.email
```

With conditional updates:
```sql
INSERT INTO users (id, username, email, updated_at)
VALUES (:id, :username, :email, :updated_at)
ON CONFLICT (id) DO UPDATE SET
    username = EXCLUDED.username,
    email = EXCLUDED.email,
    updated_at = EXCLUDED.updated_at
WHERE EXCLUDED.updated_at > users.updated_at
```

[Learn more about PostgreSQL implementation](docs/postgresql.md)

## Best Practices

1. **Define Appropriate Constraints**: Ensure that your tables have appropriate unique or primary key constraints for the columns you want to use in the ON clause.
2. **Use Batch Operations**: When upserting multiple entities, use the `upsertAll` method to take advantage of batch operation support.
3. **Consider Performance**: For large datasets, consider using custom methods with specific ON clauses and ignored fields to optimize performance.

### Contributing

For information on how to contribute to this project, including deploying to Maven Central and using
GitHub Actions for deployment, please see the [CONTRIBUTING.md](CONTRIBUTING.md) file.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
