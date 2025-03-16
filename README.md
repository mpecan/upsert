# Upsert Repository

A Spring Data JPA extension that provides upsert capabilities for repositories.

## Features

- Upsert a single entity or a list of entities
- Support for custom ON clauses and ignored fields
- Compatible with Spring Data JPA repositories

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

The upsert functionality is implemented using database-specific SQL syntax:
- For PostgreSQL, it uses the `INSERT ... ON CONFLICT ... DO UPDATE` syntax
- For MySQL, it uses the `INSERT ... ON DUPLICATE KEY UPDATE` syntax

The implementation automatically detects the database type and uses the appropriate syntax.

## License

This project is licensed under the MIT License - see the LICENSE file for details.