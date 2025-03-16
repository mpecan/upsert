# Spring Data Upsert Extension

A Spring Data extension that allows the use of upsert operations in both PostgreSQL and MySQL databases.

## Overview

This library provides a way to perform upsert operations (insert or update) in Spring Data repositories. It supports both PostgreSQL and MySQL databases, and automatically selects the appropriate SQL syntax based on the database being used.

## Features

- Support for both PostgreSQL and MySQL databases
- Automatic detection of the database type
- Custom annotations for marking key and value fields
- Support for JPA annotations (@Id, @EmbeddedId) for key fields
- Automatic detection of value fields when using JPA entities
- Batch upsert operations with `upsertAll` method
- Spring Boot auto-configuration for easy setup
- Repository extension for direct integration with Spring Data repositories

## Usage

### 1. Add the dependency to your project

```kotlin
dependencies {
    implementation("si.pecan:upsert:0.0.1-SNAPSHOT")
}
```

### 2. Configure your entity

You can use either custom annotations or JPA annotations to configure your entity for upsert operations.

#### Using custom annotations

```kotlin
import si.pecan.upsert.annotation.UpsertKey
import si.pecan.upsert.annotation.UpsertValue

data class MyEntity(
    @UpsertKey
    val id: Long,

    @UpsertValue
    val name: String,

    @UpsertValue
    val description: String? = null,

    @UpsertValue
    val active: Boolean = true
)
```

#### Using JPA annotations

```kotlin
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "my_entity")
data class MyEntity(
    @Id
    val id: Long,

    val name: String,

    val description: String? = null,

    val active: Boolean = true
)
```

When using JPA annotations:
- Fields annotated with `@Id` or `@EmbeddedId` are automatically used as key fields
- All non-key fields are automatically used as value fields

### 3. Use the UpsertOperations interface

```kotlin
@Service
class MyService(private val upsertOperations: UpsertOperations) {

    fun saveEntity(entity: MyEntity): Int {
        return upsertOperations.upsert(entity, "my_entity")
    }

    fun saveEntities(entities: List<MyEntity>): Int {
        return upsertOperations.upsertAll(entities, "my_entity")
    }
}
```

### 4. Use the Repository extension

You can also use the UpsertRepository interface to add upsert functionality to your Spring Data repositories:

```kotlin
import si.pecan.upsert.repository.UpsertRepository

interface MyEntityRepository : UpsertRepository<MyEntity, Long> {
    // Other repository methods...
}
```

Then you can use the repository to perform upsert operations:

```kotlin
@Service
class MyService(private val repository: MyEntityRepository) {

    fun saveEntity(entity: MyEntity): Int {
        return repository.upsert(entity)
    }

    fun saveEntities(entities: List<MyEntity>): Int {
        return repository.upsertAll(entities)
    }
}
```

### 5. Use the extension functions

Alternatively, you can use the extension functions to add upsert functionality to any Spring Data repository:

```kotlin
import si.pecan.upsert.repository.upsert
import si.pecan.upsert.repository.upsertAll
import org.springframework.data.repository.CrudRepository

@Service
class MyService(
    private val repository: CrudRepository<MyEntity, Long>,
    private val upsertOperations: UpsertOperations,
    private val entityManager: EntityManager
) {

    fun saveEntity(entity: MyEntity): Int {
        return repository.upsert(entity, upsertOperations, entityManager)
    }

    fun saveEntities(entities: List<MyEntity>): Int {
        return repository.upsertAll(entities, upsertOperations, entityManager)
    }
}
```

### 6. Auto-configuration

The library provides auto-configuration for Spring Boot applications. When you add the library to your classpath, it will automatically configure the necessary beans based on the available dependencies.

To use auto-configuration, you don't need to add any configuration to your application. Just add the dependency to your project, and the library will automatically configure itself.

If you need to customize the configuration, you can provide your own beans with the same names as the auto-configured beans, and the auto-configuration will back off.

```kotlin
@Configuration
class MyCustomConfiguration {

    @Bean
    fun upsertDialectFactory(dataSource: DataSource): UpsertDialectFactory {
        // Custom implementation
        return CustomUpsertDialectFactory(dataSource)
    }

    @Bean
    fun upsertDialect(dialectFactory: UpsertDialectFactory): UpsertDialect {
        // Custom implementation
        return CustomUpsertDialect()
    }

    @Bean
    fun upsertOperations(jdbcTemplate: JdbcTemplate, dialect: UpsertDialect): UpsertOperations {
        // Custom implementation
        return CustomUpsertOperations(jdbcTemplate, dialect)
    }
}
```

## Implementation Details

The library consists of the following components:

1. **Annotations**: `@Upsert`, `@UpsertKey`, and `@UpsertValue` for marking methods and fields for upsert operations.
2. **Dialects**: `PostgreSqlUpsertDialect` and `MySqlUpsertDialect` for generating database-specific SQL.
3. **Processor**: `UpsertProcessor` for processing entity classes and generating SQL.
4. **Operations**: `UpsertOperations` interface and `JdbcUpsertOperations` implementation for executing upsert operations.
5. **Repository**: `UpsertRepository` interface and `UpsertRepositoryImpl` implementation for integrating with Spring Data repositories.
6. **Extensions**: Extension functions for adding upsert functionality to any Spring Data repository.
7. **Configuration**: `UpsertConfiguration` for manual configuration and `UpsertAutoConfiguration` for automatic configuration based on classpath libraries.

## Database Support

### PostgreSQL

Uses the `INSERT ... ON CONFLICT ... DO UPDATE` syntax:

```sql
INSERT INTO table_name (id, name, description, active) 
VALUES (?, ?, ?, ?) 
ON CONFLICT (id) DO UPDATE SET 
name = EXCLUDED.name, 
description = EXCLUDED.description, 
active = EXCLUDED.active
```

### MySQL

Uses the `INSERT ... ON DUPLICATE KEY UPDATE` syntax:

```sql
INSERT INTO table_name (id, name, description, active) 
VALUES (?, ?, ?, ?) 
ON DUPLICATE KEY UPDATE 
name = VALUES(name), 
description = VALUES(description), 
active = VALUES(active)
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
