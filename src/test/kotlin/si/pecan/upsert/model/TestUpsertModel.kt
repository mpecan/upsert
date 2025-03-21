package si.pecan.upsert.model

import jakarta.annotation.Generated
import jakarta.persistence.PersistenceUnitUtil
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.metamodel.Attribute
import jakarta.persistence.metamodel.EntityType
import jakarta.persistence.metamodel.Metamodel
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.jdbc.core.StatementCreatorUtils
import si.pecan.upsert.dialect.ColumnInfo

/**
 * A simplified version of UpsertModel for testing purposes.
 * This class doesn't rely on JpaMetamodelEntityInformation, which is difficult to mock.
 */
class TestUpsertModel(
    private val tableName: String,
    private val columns: List<ColumnInfo>,
    private val idColumns: List<ColumnInfo>? = null,
    private val idColumn: ColumnInfo? = null,
    private val uniqueColumns: List<ColumnInfo> = emptyList()
) {
    /**
     * Validate the upsert query.
     * This method checks that the columns and values are present in the entity. It also checks that the on columns are part of the unique constraint or the primary key.
     *
     * @param values The values to upsert
     * @param onColumns The columns to use for the ON clause
     * @param updateColumns The columns to update on conflict
     */
    fun validateUpsertQuery(
        values: List<ColumnInfo>,
        onColumns: List<ColumnInfo>,
        updateColumns: List<ColumnInfo>
    ) {
        val missingColumns = onColumns.filter { it !in columns }
        require(missingColumns.isEmpty()) { "Columns $missingColumns are not present in entity" }
        // Check that on columns are either part of the unique constraint or the primary key
        val missingUniqueColumns = onColumns.filter {
            it !in uniqueColumns && it !in (idColumns ?: listOfNotNull(idColumn))
        }
        require(missingUniqueColumns.isEmpty()) {
            "Columns $missingUniqueColumns are not part of the unique constraint or primary key"
        }
        val missingValues = values.filter { it !in columns }
        require(missingValues.isEmpty()) {
            "Columns $missingValues are not present in entity"
        }

        val missingUpdateColumns = updateColumns.filter { it !in columns }
        require(missingUpdateColumns.isEmpty()) {
            "Columns $missingUpdateColumns are not present in entity"
        }

        val incorrectUpdateColumns = updateColumns.filter { it in onColumns }
        require(incorrectUpdateColumns.isEmpty()) {
            "Columns $incorrectUpdateColumns are part of the ON clause and cannot be updated"
        }
    }

    /**
     * Get the default columns for the upsert query.
     * This returns the columns that should be used as keys in the upsert query.
     * @return The list of default columns
     */
    fun getDefaultOnColumns(): List<ColumnInfo> {
        return idColumns ?: listOfNotNull(idColumn)
    }

    /**
     * Get the default values for the upsert query.
     * This returns the columns that should be used as values in the upsert query.
     * @return The list of default values
     */
    fun getDefaultValues(): List<ColumnInfo> {
        return columns.filter { !it.generated }
    }

    /**
     * Get the default update columns for the upsert query.
     * This returns the columns that should be updated during the upsert operation.
     * @return The list of default update columns
     */
    fun getDefaultUpdateColumns(): List<ColumnInfo> {
        return columns.filter { it !in (idColumns ?: listOfNotNull(idColumn)) }
    }

    /**
     * Get the table name.
     * @return The table name
     */
    fun getTableName(): String {
        return tableName
    }

    /**
     * Get the value columns for the upsert query.
     * @param fields The fields to include in the values
     * @return The list of value columns
     */
    fun getValueColumns(fields: List<String>): List<ColumnInfo> {
        if (fields.isEmpty()) {
            return getDefaultValues()
        }
        return columns.filter { it.name in fields }
    }

    /**
     * Get the ON columns for the upsert query.
     * @param fields The fields to include in the ON clause
     * @return The list of ON columns
     */
    fun getOnColumns(fields: List<String>): List<ColumnInfo> {
        if (fields.isEmpty()) {
            return getDefaultOnColumns()
        }
        return columns.filter { it.name in fields }
    }

    /**
     * Create an UpsertInstance.
     * @param tableName The table name
     * @param onColumns The columns to use for the ON clause
     * @param values The values to upsert
     * @param updateColumns The columns to update on conflict
     * @return The UpsertInstance
     */
    fun createUpsertInstance(
        tableName: String,
        onColumns: List<String>?,
        values: List<String>?,
        updateColumns: List<String>?,
    ): UpsertInstance {
        return UpsertInstance(
            tableName,
            onColumns?.let { getOnColumns(it) } ?: getDefaultOnColumns(),
            values?.let { getValueColumns(it) } ?: getDefaultValues(),
            updateColumns?.let { getUpdateColumns(it) } ?: getDefaultUpdateColumns()
        )
    }

    /**
     * Get the update columns for the upsert query.
     * @param fields The fields to include in the update
     * @return The list of update columns
     */
    private fun getUpdateColumns(fields: List<String>): List<ColumnInfo> {
        return columns.filter { it.name in fields }
    }

    /**
     * UpsertInstance class.
     * This class represents an instance of an upsert operation.
     */
    inner class UpsertInstance(
        val tableName: String,
        val onColumns: List<ColumnInfo>,
        val values: List<ColumnInfo>,
        val updateColumns: List<ColumnInfo>,
    ) {
        init {
            this@TestUpsertModel.validateUpsertQuery(values, onColumns, updateColumns)
        }
    }
}
