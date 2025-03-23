package io.github.mpecan.upsert.model

data class ColumnInfo(
    val name: String,
    val fieldName: String,
    val clazz: Class<*>,
    val sqlTypeId: Int,
    val generated: Boolean = false
)