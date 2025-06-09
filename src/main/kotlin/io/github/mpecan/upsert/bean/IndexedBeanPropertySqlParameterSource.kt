package io.github.mpecan.upsert.bean

import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource

class IndexedBeanPropertySqlParameterSource(
    private val beanList: List<ExtendedBeanPropertySqlParameterSource>
) :
    AbstractSqlParameterSource() {
    override fun getParameterNames(): Array<String> {
        return beanList.flatMapIndexed { index, bean ->
            bean.parameterNames.map { getIndexedPropertyName(it, index) }
        }.toTypedArray()
    }

    override fun getValue(propertyName: String): Any? {
        val (beanName, beanIndex) = splitIndexedPropertyName(propertyName)
        val value = beanList[beanIndex].getValue(beanName)
        // Debug logging for all parameters in conditional upserts
        if (beanName == "updatedAt" || beanName == "version" || propertyName.contains("updatedAt") || propertyName.contains("version")) {
            println("[DEBUG_LOG] IndexedBeanPropertySqlParameterSource.getValue: propertyName=$propertyName, beanName=$beanName, value=$value")
        }
        return value
    }

    override fun hasValue(propertyName: String): Boolean {
        val (beanName, beanIndex) = splitIndexedPropertyName(propertyName)
        return beanList[beanIndex].hasValue(beanName)
    }

    override fun getSqlType(propertyName: String): Int {
        val (beanName, beanIndex) = splitIndexedPropertyName(propertyName)
        return beanList[beanIndex].getSqlType(beanName)
    }

    override fun getTypeName(propertyName: String): String? {
        val (beanName, beanIndex) = splitIndexedPropertyName(propertyName)
        return beanList[beanIndex].getTypeName(beanName)
    }

    private fun splitIndexedPropertyName(propertyName: String): Pair<String, Int> {
        // Handle property names with underscores by finding the last underscore
        val lastUnderscoreIndex = propertyName.lastIndexOf("_")
        require(lastUnderscoreIndex != -1) { "Invalid indexed property name: $propertyName" }
        val beanName = propertyName.substring(0, lastUnderscoreIndex)
        val index = propertyName.substring(lastUnderscoreIndex + 1).toInt() - 1
        return Pair(beanName, index)
    }

    private fun getIndexedPropertyName(propertyName: String, index: Int): String {
        return "${propertyName}_${index + 1}"
    }
}