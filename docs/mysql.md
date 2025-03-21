# MySQL Upsert Implementation

This document provides detailed information about the MySQL implementation of the upsert
functionality in this library.

## Overview

The MySQL implementation uses the `INSERT ... ON DUPLICATE KEY UPDATE` syntax to perform upsert
operations. This syntax allows for inserting a new row if it doesn't exist, or updating an existing
row if it does.

## Implementation Details

The MySQL dialect is implemented in the `MySqlUpsertDialect` class, which extends the
`UpsertDialect` interface. The key components of this implementation include:

1. **SQL Generation**: The `generateBatchUpsertSql` method generates the SQL statement for batch
   upsert operations.
2. **Data Execution**: The `upsertData` method executes the upsert operation and handles generated
   keys.
3. **Type Conversion**: The `convertToFieldType` method handles type conversions for generated keys.

## SQL Syntax

The MySQL upsert syntax follows this pattern:

```sql
INSERT INTO table_name (column1, column2, ...)
VALUES (value1, value2, ...),
       (value1, value2, ...), ...
    ON DUPLICATE KEY
UPDATE
    column1 =
VALUES (column1), column2 =
VALUES (column2), ...
```

This syntax relies on the presence of a unique or primary key constraint on the table. When a
duplicate key is encountered, the `ON DUPLICATE KEY UPDATE` clause is triggered, and the specified
columns are updated with the values that would have been inserted.

## Features

### Batch Operations

The MySQL implementation supports batch operations by including multiple value sets in a single
query:

```sql
INSERT INTO users (id, username, email)
VALUES (:id_1, :username_1, :email_1),
       (:id_2, :username_2, :email_2), ...
    ON DUPLICATE KEY
UPDATE
    username =
VALUES (username), email =
VALUES (email)
```

This approach is more efficient than executing multiple individual queries.

### Generated Keys

The implementation handles generated keys (such as auto-increment IDs) by updating the entity
objects after insertion. This ensures that the entity objects have the correct values for generated
fields.

### Type Conversion

The implementation leverages the Spring ConfigurablePropertyAccessor for type conversion when
setting generated keys on entity objects. This allows for flexible handling of different data types.

## Usage Examples

### Basic Upsert

```kotlin
// Define your entity
data class User(
    val id: Long? = null,
    val username: String,
    val email: String
)

// Define your repository
interface UserRepository : UpsertRepository<User, Long> {
    // Standard Spring Data JPA methods
}

// Use the repository
val user = User(username = "john", email = "john@example.com")
userRepository.upsert(user)
```

### Batch Upsert

```kotlin
// Define your entities
val users = listOf(
    User(username = "john", email = "john@example.com"),
    User(username = "jane", email = "jane@example.com")
)

// Use the repository
userRepository.upsertAll(users)
```

### Custom ON Clause

```kotlin
interface UserRepository : UpsertRepository<User, Long> {
    // Upsert using username as the ON clause
    fun upsertOnUsername(user: User): Int
}

// Use the repository
val user = User(username = "john", email = "updated-email@example.com")
userRepository.upsertOnUsername(user)
```

### Ignoring Fields

```kotlin
interface UserRepository : UpsertRepository<User, Long> {
    // Upsert using username as the ON clause and ignoring updatedAt field
    fun upsertOnUsernameIgnoringUpdatedAt(user: User): Int
}

// Use the repository
val user = User(username = "john", email = "john@example.com", updatedAt = LocalDateTime.now())
userRepository.upsertOnUsernameIgnoringUpdatedAt(user)
```

### Ignoring All Fields (Insert-Only Mode)

When you want to only insert new records and not update existing ones, you can use the
`IgnoringAllFields` suffix in your method name:

```kotlin
interface UserRepository : UpsertRepository<User, Long> {
    // Upsert using id as the ON clause and ignoring all fields
    // This will only insert new rows and not update existing ones
    fun upsertOnIdIgnoringAllFields(user: User): Int
}

// Use the repository
val user = User(id = 1, username = "john", email = "john@example.com")
userRepository.upsertOnIdIgnoringAllFields(user)
```

In MySQL, this is implemented by using a dummy update that effectively does nothing (setting a
column to its current value). This ensures that existing records are not modified while still
allowing new records to be inserted.

## Limitations

1. **Unique Key Requirement**: The MySQL implementation relies on the presence of a unique or
   primary key constraint on the table. If no such constraint exists, the upsert operation will not
   work as expected.

2. **Generated Keys Handling**: The implementation attempts to handle generated keys by checking
   various possible key names. However, this approach may not work for all database configurations.

## Best Practices

1. **Define Appropriate Indexes**: Ensure that your tables have appropriate unique or primary key
   constraints for the columns you want to use in the ON clause.

2. **Use Batch Operations**: When upserting multiple entities, use the `upsertAll` method or a
   custom batch upsert method to take advantage of the batch operation support.

3. **Handle Generated Keys**: If your entities have generated fields (such as auto-increment IDs),
   be aware that the implementation will attempt to update these fields after insertion.
