package io.github.mpecan.upsert.model

/**
 * Model class for upsert operations.
 * This class uses a UpsertModelMetadataProvider to get metadata about the entity.
 */
class UpsertModel(
    private val metadataProvider: UpsertModelMetadataProvider
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
        val columns = metadataProvider.getColumns()
        val uniqueColumns = metadataProvider.getUniqueColumns()
        val idColumns = metadataProvider.getIdColumns()
        val idColumn = metadataProvider.getIdColumn()

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
        return metadataProvider.getIdColumns() ?: listOfNotNull(metadataProvider.getIdColumn())
    }

    /**
     * Get the default values for the upsert query.
     * This returns the columns that should be used as values in the upsert query.
     * @return The list of default values
     */
    fun getDefaultValues(): List<ColumnInfo> {
        return metadataProvider.getColumns()
    }

    /**
     * Get the default update columns for the upsert query.
     * This returns the columns that should be updated during the upsert operation.
     * @return The list of default update columns
     */
    fun getDefaultUpdateColumns(): List<ColumnInfo> {
        val idColumns = metadataProvider.getIdColumns()
        val idColumn = metadataProvider.getIdColumn()
        return metadataProvider.getColumns()
            .filter { it !in (idColumns ?: listOfNotNull(idColumn)) }
    }

    /**
     * Get the table name.
     * @return The table name
     */
    fun getTableName(): String {
        return metadataProvider.getTableName()
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
        return metadataProvider.getColumns().filter { it.name in fields }
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
        return metadataProvider.getColumns().filter { it.name in fields }
    }


    /**
     * Create an UpsertInstance.
     * @param onColumns The columns to use for the ON clause
     * @param values The values to upsert
     * @param ignoreColumns The columns to ignore during updates
     * @param ignoreAllFields Whether to ignore all fields during updates
     * @return The UpsertInstance
     */
    fun createUpsertInstance(
        onColumns: List<String>? = null,
        values: List<String>? = null,
        ignoreColumns: List<String>? = null,
        ignoreAllFields: Boolean? = null,
    ): UpsertInstance {
        val onColumnsInUse = onColumns?.let { getOnColumns(it) } ?: getDefaultOnColumns()
        val updateColumns = when {
            ignoreAllFields == true -> emptyList()
            ignoreColumns == null -> getDefaultValues().filter {
                it !in onColumnsInUse
            }
            ignoreColumns.isNotEmpty() -> getDefaultValues().filter {
                it.name !in ignoreColumns && it !in onColumnsInUse
            }
            else -> getDefaultUpdateColumns()
        }
        return UpsertInstance(
            metadataProvider.getTableName(),
            onColumnsInUse,
            values?.let { getValueColumns(it) } ?: getDefaultValues(),
            updateColumns
        )
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
            this@UpsertModel.validateUpsertQuery(values, onColumns, updateColumns)
        }

        fun withoutValueColumns(columnsToExclude: List<ColumnInfo>): UpsertInstance {
            return UpsertInstance(
                tableName,
                onColumns,
                values.filter { it !in columnsToExclude },
                updateColumns
            )
        }

        fun forFirstUniqueConstraint(): UpsertInstance {
            if (metadataProvider.getUniqueConstraints().isEmpty()) {
                throw IllegalStateException("No unique constraints found")
            }
            val uniqueColumns = metadataProvider.getUniqueConstraints().first()
            val updateColumns = updateColumns.filter { it !in uniqueColumns }
            return UpsertInstance(
                tableName,
                uniqueColumns,
                values,
                updateColumns
            )
        }
    }
}
