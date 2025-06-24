package io.github.mpecan.upsert.type.json.test

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

/**
 * Test entity with various JSON field types for testing JSON mapping functionality.
 */
@Entity
@Table(
    name = "test_json_entity",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["name"])
    ]
)
data class TestJsonEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val name: String,

    // Explicit JSON field via column definition
    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    val attributes: Map<String, String>,

    // Collection type that should be treated as JSON
    @JdbcTypeCode(SqlTypes.JSON)
    val tags: List<String>,

    // Nested custom class that should be serialized as JSON
    @JdbcTypeCode(SqlTypes.JSON)
    val metadata: TestMetadata,

    // Standard JPA type (not JSON)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Nested data class for JSON serialization testing.
 */
data class TestMetadata(
    val version: String,
    val description: String,
    val active: Boolean,
    val settings: Map<String, String>,
    val scores: List<Int>,
    val nested: TestNestedData? = null
)

/**
 * Second-level nested data class for testing deep object serialization.
 */
data class TestNestedData(
    val id: String,
    val value: Double
)