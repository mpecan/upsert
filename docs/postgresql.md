# PostgreSQL Upsert Implementation

This document provides detailed information about the PostgreSQL implementation of the upsert functionality in this library.

## Overview

The PostgreSQL implementation uses the `INSERT ... ON CONFLICT ... DO UPDATE` syntax to perform upsert operations. This syntax, introduced in PostgreSQL 9.5, allows for inserting a new row if it doesn't exist, or updating an existing row if it does.

## Implementation Details

The PostgreSQL dialect is implemented in the `PostgreSqlUpsertDialect` class, which extends the `UpsertDialect` interface. The key components of this implementation include:

1. **SQL Generation**: The `generateBatchUpsertSql` method generates the SQL statement for batch upsert operations.
2. **Data Execution**: The `upsertData` method executes the upsert operation and handles generated keys.
3. **Generated Keys Handling**: The `updateGeneratedKeys` method updates entity objects with generated keys.
4. **Type Conversion**: The `convertToFieldType` method handles type conversions for generated keys.
5. **Optimized Batch Support**: The `supportsOptimizedBatch` method indicates that PostgreSQL supports optimized batch operations.

## SQL Syntax

The PostgreSQL upsert syntax follows this pattern:

```sql
INSERT INTO table_name (column1, column2, ...)
VALUES (value1, value2, ...)
ON CONFLICT (key_column1, key_column2, ...)
DO UPDATE SET
    column1 = EXCLUDED.column1,
    column2 = EXCLUDED.column2,
    ...
```

This syntax allows for specifying exactly which columns should be used for conflict detection (the ON CONFLICT clause) and which columns should be updated when a conflict occurs (the DO UPDATE SET clause).

## Features

### Conflict Detection

PostgreSQL allows for explicit conflict detection using the `ON CONFLICT` clause. This can be based on:

1. **Primary Key**: The primary key of the table
2. **Unique Constraint**: Any unique constraint on the table
3. **Unique Index**: Any unique index on the table

This flexibility allows for more control over when an update should occur.

### EXCLUDED Table Reference

PostgreSQL provides the `EXCLUDED` table reference, which contains the values that would have been inserted if the conflict hadn't occurred. This makes it easy to reference the new values in the update clause:

```sql
DO UPDATE SET
    column1 = EXCLUDED.column1,
    column2 = EXCLUDED.column2
```

### Handling Entities With and Without IDs

The PostgreSQL implementation has sophisticated handling for entities with and without IDs:

1. **Entities with IDs**: These are processed using the primary key for conflict detection.
2. **Entities without IDs**: These are processed using unique constraints for conflict detection.

This approach ensures that entities are correctly upserted regardless of whether they have an ID.

### Optimized Batch Operations

PostgreSQL supports optimized batch operations, as indicated by the `supportsOptimizedBatch` method. This allows for more efficient processing of large batches of entities.

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

When you want to only insert new records and not update existing ones, you can use the `IgnoringAllFields` suffix in your method name:

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

In PostgreSQL, this is implemented using the `DO NOTHING` option, which simply ignores conflicts without performing any updates. This ensures that existing records are not modified while still allowing new records to be inserted.

### Using Multiple Fields for Conflict Detection

```kotlin
interface UserRepository : UpsertRepository<User, Long> {
    // Upsert using username and email as the ON clause
    fun upsertOnUsernameAndEmail(user: User): Int
}

// Use the repository
val user = User(username = "john", email = "john@example.com")
userRepository.upsertOnUsernameAndEmail(user)
```

## Advanced Features

### Conditional Updates

PostgreSQL allows for conditional updates using the `WHERE` clause in the `DO UPDATE SET` part:

```sql
INSERT INTO users (username, email, last_login)
VALUES (:username, :email, :last_login)
ON CONFLICT (username) DO UPDATE SET
    email = EXCLUDED.email,
    last_login = EXCLUDED.last_login
    WHERE users.last_login < EXCLUDED.last_login
```

This example only updates the `last_login` field if the new value is more recent than the existing value.

### DO NOTHING Option

PostgreSQL also supports the `DO NOTHING` option, which simply ignores conflicts without performing an update:

```sql
INSERT INTO users (username, email)
VALUES (:username, :email)
ON CONFLICT (username) DO NOTHING
```

This can be useful when you want to insert records only if they don't already exist. This is how the `IgnoringAllFields` feature is implemented in PostgreSQL - when all update columns are ignored, the dialect automatically uses `DO NOTHING` instead of `DO UPDATE SET`.

## Limitations

1. **PostgreSQL Version Requirement**: The upsert functionality requires PostgreSQL 9.5 or later, as this is when the `ON CONFLICT` syntax was introduced.

2. **Unique Constraint Requirement**: The PostgreSQL implementation relies on the presence of a unique or primary key constraint on the table for conflict detection. If no such constraint exists, the upsert operation will not work as expected.

## Best Practices

1. **Define Appropriate Constraints**: Ensure that your tables have appropriate unique or primary key constraints for the columns you want to use in the ON CONFLICT clause.

2. **Use Batch Operations**: When upserting multiple entities, use the `upsertAll` method or a custom batch upsert method to take advantage of the optimized batch operation support.

3. **Handle Generated Keys**: If your entities have generated fields (such as auto-increment IDs), be aware that the implementation will attempt to update these fields after insertion.

4. **Consider Conditional Updates**: For more complex scenarios, consider using conditional updates to control when and how updates occur.
