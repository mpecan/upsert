package io.github.mpecan.upsert.model

import io.github.mpecan.upsert.type.TypeMapperRegistry
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PersistenceUnitUtil
import jakarta.persistence.metamodel.Metamodel
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation
import java.lang.reflect.Field

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
            val field = findFieldInClassHierarchy(entityClass, it.name)
                ?: throw NoSuchFieldException("Field '${it.name}' not found in class hierarchy of ${entityClass.name}")
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

    companion object {
        /**
         * Finds a field in the class hierarchy, including fields from @MappedSuperclass parent classes.
         * This method searches through the entire inheritance chain, starting from the given class
         * and moving up through parent classes that are annotated with @MappedSuperclass.
         *
         * @param clazz The class to start searching from
         * @param fieldName The name of the field to find
         * @return The field if found, null otherwise
         */
        internal fun findFieldInClassHierarchy(clazz: Class<*>, fieldName: String): Field? {
            var currentClass: Class<*>? = clazz

            while (currentClass != null) {
                try {
                    // Try to find the field in the current class
                    val field = currentClass.getDeclaredField(fieldName)
                    field.isAccessible = true
                    return field
                } catch (_: NoSuchFieldException) {
                    // Field not found in current class, check parent class
                    val superClass = currentClass.superclass

                    // Only continue searching if the parent class is annotated with @MappedSuperclass
                    // or if we're still in the entity hierarchy (not reached Object class)
                    currentClass = when {
                        superClass == null || superClass == Any::class.java -> null
                        superClass.isAnnotationPresent(MappedSuperclass::class.java) -> superClass
                        // Continue searching even if not @MappedSuperclass to handle complex hierarchies
                        currentClass == clazz -> superClass // Allow first level up for entity inheritance
                        else -> null
                    }
                }
            }

            return null
        }
    }
}