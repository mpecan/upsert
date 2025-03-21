package si.pecan.upsert.model

import si.pecan.upsert.dialect.ColumnInfo

/**
 * Mock implementation of UpsertModelMetadataProvider for testing.
 * This class takes all the necessary information as constructor parameters,
 * making it easy to use in tests without relying on JPA metadata.
 */
class MockUpsertModelMetadataProvider(
    private val tableName: String,
    private val columns: List<ColumnInfo>,
    private val idColumns: List<ColumnInfo>? = null,
    private val idColumn: ColumnInfo? = null,
    private val uniqueColumns: List<ColumnInfo> = emptyList(),
    private val uniqueConstraints: List<List<ColumnInfo>> = emptyList(),
    private val entityClass: Class<out Any> = Any::class.java
) : UpsertModelMetadataProvider {

    override fun getTableName(): String = tableName

    override fun getColumns(): List<ColumnInfo> = columns

    override fun getIdColumns(): List<ColumnInfo>? = idColumns

    override fun getIdColumn(): ColumnInfo? = idColumn

    override fun getUniqueColumns(): List<ColumnInfo> = uniqueColumns
    override fun getUniqueConstraints(): List<List<ColumnInfo>> = uniqueConstraints

    override fun getEntityClass(): Class<out Any> = entityClass
}