package io.github.mpecan.upsert.model

import io.github.mpecan.upsert.type.TypeMapperRegistry
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.PersistenceUnitUtil
import jakarta.persistence.metamodel.Metamodel
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation

/**
 * Implementation of UpsertModelMetadataProvider that uses JPA metadata.
 * This class extracts metadata from JPA entities for use in upsert operations.
 */
class JpaUpsertModelMetadataProvider(
    metamodel: Metamodel,
    persistenceUnitUtil: PersistenceUnitUtil,
    private val entityClass: Class<out Any>,
    private val typeMapperRegistry: TypeMapperRegistry
) : UpsertModelMetadataProvider {

    private val tableName: String
    private val entityMetadata = metamodel.entity(entityClass)
    private val uniqueColumns: Set<ColumnInfo>
    private val uniqueConstraints: Set<Set<ColumnInfo>>
    private val columns: Set<ColumnInfo>
    private val idColumns: Set<ColumnInfo>?
    private val idColumn: ColumnInfo?

    init {
        val annotations = entityClass.annotations
        val tableAnnotation =
            annotations.find { it is jakarta.persistence.Table } as jakarta.persistence.Table
        tableName = tableAnnotation.name

        val entityInformation: JpaEntityInformation<out Any, Any> =
            JpaMetamodelEntityInformation(entityClass, metamodel, persistenceUnitUtil)

        columns = entityMetadata.attributes.filter { it.isAssociation.not() }.map {
            val field = it.declaringType.javaType.getDeclaredField(it.name)
            val isGenerated = field.annotations.any { annotation -> annotation is GeneratedValue }

            val columnName = when {
                field.annotations.any { annotation -> annotation.javaClass == Column::class.java } -> {
                    field.getAnnotation(Column::class.java).name
                }
                else -> {
                    // Get the column name from the field name and transform it to snake case
                    field.name.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").lowercase()
                }
            }

            // Use the TypeMapperRegistry to get the SQL type
            val sqlType = typeMapperRegistry.getSqlTypeForField(field)

            ColumnInfo(
                columnName,
                it.name,
                it.javaType,
                sqlType,
                isGenerated
            )
        }.toSet()

        if (entityInformation.hasCompositeId()) {
            idColumns = entityInformation.idAttributeNames.mapNotNull { idAttributeName ->
                columns.find { it.name == idAttributeName }
            }.toSet()
            idColumn = null
        } else {
            idColumn = entityInformation.idAttribute?.let { idAttribute ->
                columns.find { it.name == idAttribute.name }
            }
            idColumns = null
        }

        val uniqueConstraints = tableAnnotation.uniqueConstraints

        uniqueColumns =
            uniqueConstraints.flatMap { it.columnNames.toList() }.mapNotNull { columnName ->
                columns.find { it.name == columnName }
            }.toSet()

        this.uniqueConstraints = uniqueConstraints.map {
            it.columnNames.mapNotNull { columnName ->
                columns.find { it.name == columnName }
            }.toSet()
        }.toSet()
    }

    override fun getTableName(): String = tableName

    override fun getColumns(): List<ColumnInfo> = columns.toList()

    override fun getIdColumns(): List<ColumnInfo>? = idColumns?.toList()

    override fun getIdColumn(): ColumnInfo? = idColumn

    override fun getUniqueColumns(): List<ColumnInfo> = uniqueColumns.toList()

    override fun getUniqueConstraints(): List<List<ColumnInfo>> =
        uniqueConstraints.map { it.toList() }

    override fun getEntityClass(): Class<out Any> = entityClass
}