# Upsert Repository

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
- Compatible with Spring Data JPA repositories
- Database-specific optimizations for MySQL and PostgreSQL
- Automatic handling of generated keys
- Batch operation support for improved performance

## What is Upsert?

"Upsert" is a combination of "update" and "insert" - it's an operation that will:
- Insert a new record if it doesn't exist
- Update an existing record if it does exist

This is particularly useful when you don't know whether a record exists and want to ensure it's created or updated in a single operation.

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

## Configuration

To enable upsert capabilities, you need to include the `UpsertRepositoryFactoryBean` in your Spring configuration:

```kotlin
@Configuration
@EnableJpaRepositories(
    repositoryFactoryBeanClass = UpsertRepositoryFactoryBean::class
)
class AppConfig {
    // ...
}
```

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

[Learn more about PostgreSQL implementation](docs/postgresql.md)

## Best Practices

1. **Define Appropriate Constraints**: Ensure that your tables have appropriate unique or primary key constraints for the columns you want to use in the ON clause.
2. **Use Batch Operations**: When upserting multiple entities, use the `upsertAll` method to take advantage of batch operation support.
3. **Consider Performance**: For large datasets, consider using custom methods with specific ON clauses and ignored fields to optimize performance.

## Maven Central

### Using the Library

This library is available on Maven Central. You can add it to your project using:

#### Gradle (Kotlin DSL)

```kotlin
dependencies {
   implementation("io.github.mpecan:upsert:0.0.1")
}
```

#### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'io.github.mpecan:upsert:0.0.1'
}
```

#### Maven

```xml

<dependency>
  <groupId>io.github.mpecan</groupId>
  <artifactId>upsert</artifactId>
  <version>0.0.1</version>
</dependency>
```

### Contributing

For information on how to contribute to this project, including deploying to Maven Central and using
GitHub Actions for deployment, please see the [CONTRIBUTING.md](CONTRIBUTING.md) file.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
