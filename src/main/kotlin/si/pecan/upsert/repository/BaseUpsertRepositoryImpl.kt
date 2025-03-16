package si.pecan.upsert.repository

import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.core.EntityInformation
import javax.persistence.EntityManager
import javax.persistence.Table

/**
 * Implementation of BaseUpsertRepository.
 * This class provides the implementation for the upsert and upsertAll methods.
 *
 * @param T The entity type
 * @param ID The type of the entity's ID
 */
class BaseUpsertRepositoryImpl<T : Any, ID : Any>(
    private val entityInformation: EntityInformation<T, ID>,
    private val entityManager: EntityManager
) : SimpleJpaRepository<T, ID>(entityInformation.javaType, entityManager), BaseUpsertRepository<T, ID> {

    private lateinit var upsertOperations: UpsertOperations

    // Cache for table names by entity class
    private val tableNameCache = mutableMapOf<Class<*>, String>()

    /**
     * Set the UpsertOperations instance.
     * This method is called by the UpsertRepositoryFactoryBean.
     *
     * @param upsertOperations The UpsertOperations instance
     */
    fun setUpsertOperations(upsertOperations: UpsertOperations) {
        this.upsertOperations = upsertOperations
        
        // Initialize the upsert operations with entity class and ID class
        val entityClass = entityInformation.javaType
        val idClass = entityInformation.idType
        val tableName = getTableName(entityClass)
        
        upsertOperations.initialize(entityClass, idClass, tableName)
    }

    /**
     * Perform an upsert operation for the given entity.
     *
     * @param entity The entity to upsert
     * @return The number of rows affected
     */
    override fun upsert(entity: T): Int {
        val tableName = getTableName(entity.javaClass)
        return upsertOperations.upsert(entity, tableName)
    }

    /**
     * Perform an upsert operation for the given list of entities.
     *
     * @param entities The list of entities to upsert
     * @return The total number of rows affected
     */
    override fun upsertAll(entities: List<T>): Int {
        if (entities.isEmpty()) {
            return 0
        }

        val tableName = getTableName(entities.first().javaClass)
        return upsertOperations.upsertAll(entities, tableName)
    }

    /**
     * Perform an upsert operation for the given entity with custom ON clause and ignored fields.
     *
     * @param entity The entity to upsert
     * @param onFields The fields to use for the ON clause
     * @param ignoredFields The fields to ignore during updates
     * @param ignoreAllFields Whether to ignore all fields during updates
     * @return The number of rows affected
     */
    override fun upsert(
        entity: T,
        onFields: List<String>,
        ignoredFields: List<String>,
        ignoreAllFields: Boolean
    ): Int {
        val tableName = getTableName(entity.javaClass)
        return upsertOperations.upsert(entity, tableName, onFields, ignoredFields, ignoreAllFields)
    }

    /**
     * Perform an upsert operation for the given list of entities with custom ON clause and ignored fields.
     *
     * @param entities The list of entities to upsert
     * @param onFields The fields to use for the ON clause
     * @param ignoredFields The fields to ignore during updates
     * @param ignoreAllFields Whether to ignore all fields during updates
     * @return The total number of rows affected
     */
    override fun upsertAll(
        entities: List<T>,
        onFields: List<String>,
        ignoredFields: List<String>,
        ignoreAllFields: Boolean
    ): Int {
        if (entities.isEmpty()) {
            return 0
        }

        val tableName = getTableName(entities.first().javaClass)
        return upsertOperations.upsertAll(entities, tableName, onFields, ignoredFields, ignoreAllFields)
    }

    /**
     * Get the table name for the given entity class.
     * Uses a cache to avoid repeated lookups.
     *
     * @param entityClass The entity class
     * @return The table name
     */
    private fun getTableName(entityClass: Class<*>): String {
        // Check the cache first
        return tableNameCache.getOrPut(entityClass) {
            // Try to get the table name from @Table annotation
            val tableAnnotation = entityClass.getAnnotation(Table::class.java)
            if (tableAnnotation != null && tableAnnotation.name.isNotBlank()) {
                return@getOrPut tableAnnotation.name
            }

            // Try to get the table name from JPA entity metadata
            val entities = entityManager.metamodel.entities
            for (entity in entities) {
                if (entity.javaType == entityClass) {
                    return@getOrPut entity.name.lowercase()
                }
            }

            // Fallback to class name
            entityClass.simpleName.lowercase()
        }
    }
}