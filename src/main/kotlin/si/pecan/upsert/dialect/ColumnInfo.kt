package si.pecan.upsert.dialect

data class ColumnInfo(
    val name: String,
    val fieldName: String,
    val generated: Boolean = false
)
