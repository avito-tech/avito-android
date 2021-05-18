package com.avito.android

/**
 * @param name в формате pa.ck.age.Class
 */
data class AnnotationData(
    val name: String,
    val values: Map<String, Any?>
) {

    fun getStringValue(key: String): String? = values[key]?.toString()

    fun getIntValue(key: String): Int? = values[key] as? Int

    @Suppress("UNCHECKED_CAST")
    fun getStringArrayValue(key: String): List<String>? = values[key] as? List<String>

    @Suppress("UNCHECKED_CAST")
    fun getIntArrayValue(key: String): List<Int>? = values[key] as? List<Int>

    @Suppress("UNCHECKED_CAST")
    fun getEnumValue(key: String): String? = values[key] as? String
}
