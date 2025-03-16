package si.pecan.upsert.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.core.RepositoryMetadata
import javax.annotation.PostConstruct
import javax.persistence.EntityManager
import javax.persistence.Table

/**
 * Implementation of UpsertRepository.
 * This class provides the implementation for the upsert and upsertAll methods.
 *
 * @param T The entity type
 * @param ID The type of the entity's ID
 */
class UpsertRepositoryImpl<T : Any, ID>(
    private val metadata: RepositoryMetadata? = null
) : UpsertRepository<T, ID> {

    @Autowired
    private lateinit var upsertOperations: UpsertOperations

    @Autowired
    private lateinit var entityManager: EntityManager

    // Cache for table names by entity class
    private val tableNameCache = mutableMapOf<Class<*>, String>()

    // Store the entity class and ID class
    private var entityClass: Class<*>? = null
    private var idClass: Class<*>? = null

    /**
     * Initialize the repository with entity class and ID class from metadata.
     * This method is called by Spring after all dependencies are injected.
     */
    @PostConstruct
    fun init() {
        if (metadata != null) {
            // Extract entity class and ID class from metadata
            entityClass = metadata.domainType
            idClass = metadata.idType

            // Get the table name for the entity class
            val tableName = getTableName(entityClass!!)

            // Initialize the upsert operations with entity class and ID class
            upsertOperations.initialize(entityClass!!, idClass!!, tableName)
        }
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
        return upsertOperations.upsert(
            entity,
            getTableName(entity.javaClass),
            onFields,
            ignoredFields,
            ignoreAllFields
        )
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

        return upsertOperations.upsertAll(
            entities,
            getTableName(entities[0].javaClass),
            onFields,
            ignoredFields,
            ignoreAllFields
        )
    }

    /**
     * Get the table name for the given entity class.
     * Uses a cache to avoid repeated lookups.
     *
     * @param entityClass The entity class
     * @return The table name
     */
    fun getTableName(entityClass: Class<*>): String {
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
