package io.github.mpecan.upsert.entity

import jakarta.persistence.*

/**
 * Test entity using JPA annotations for upsert operations with a custom converter.
 */
@Entity
@Table(name = "jpa_test_entity_with_converter")
data class JpaTestEntityWithConverter(
    @Id
    val id: Long,

    val name: String,

    @Column(name = "json_data")
    @Convert(converter = JsonDataConverter::class)
    val jsonData: JsonData,

    val active: Boolean = true
)

/**
 * Simple data class to be serialized as JSON.
 */
data class JsonData(
    val key: String,
    val value: String
)

/**
 * Custom converter for JsonData.
 * Converts JsonData to a JSON string and vice versa.
 */
class JsonDataConverter : AttributeConverter<JsonData, String> {
    override fun convertToDatabaseColumn(attribute: JsonData?): String? {
        if (attribute == null) {
            return null
        }
        return """{"key":"${attribute.key}","value":"${attribute.value}"}"""
    }

    override fun convertToEntityAttribute(dbData: String?): JsonData? {
        if (dbData == null) {
            return null
        }
        // Simple parsing for testing purposes
        val keyRegex = """"key":"([^"]+)"""".toRegex()
        val valueRegex = """"value":"([^"]+)"""".toRegex()

        val keyMatch = keyRegex.find(dbData)
        val valueMatch = valueRegex.find(dbData)

        if (keyMatch != null && valueMatch != null) {
            val key = keyMatch.groupValues[1]
            val value = valueMatch.groupValues[1]
            return JsonData(key, value)
        }

        return null
    }
}
