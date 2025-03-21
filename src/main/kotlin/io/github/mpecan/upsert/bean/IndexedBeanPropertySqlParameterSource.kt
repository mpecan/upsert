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
        return beanList[beanIndex].getValue(beanName)
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
        val parts = propertyName.split("_")
        return Pair(parts[0], parts[1].toInt() - 1)
    }

    private fun getIndexedPropertyName(propertyName: String, index: Int): String {
        return "${propertyName}_$index"
    }
}