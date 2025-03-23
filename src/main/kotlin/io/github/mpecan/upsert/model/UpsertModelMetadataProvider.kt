package io.github.mpecan.upsert.model

/**
 * Interface for providing metadata needed by UpsertModel.
 * This interface encapsulates the parts of UpsertModel that are hard to mock in tests.
 */
interface UpsertModelMetadataProvider {
    /**
     * Get the table name for the entity.
     * @return The table name
     */
    fun getTableName(): String
    
    /**
     * Get all columns for the entity.
     * @return The list of columns
     */
    fun getColumns(): List<ColumnInfo>
    
    /**
     * Get the ID columns for the entity.
     * @return The list of ID columns, or null if the entity has a single ID column
     */
    fun getIdColumns(): List<ColumnInfo>?
    
    /**
     * Get the ID column for the entity.
     * @return The ID column, or null if the entity has composite ID
     */
    fun getIdColumn(): ColumnInfo?
    
    /**
     * Get the unique columns for the entity.
     * @return The list of unique columns
     */
    fun getUniqueColumns(): List<ColumnInfo>

    /**
     * Get the unique constraints for the entity.
     * @return The list of unique constraints
     */
    fun getUniqueConstraints(): List<List<ColumnInfo>>
    
    /**
     * Get the entity class.
     * @return The entity class
     */
    fun getEntityClass(): Class<out Any>
}