package com.avito.android

/**
 * @param name в формате pa.ck.age.Class
 */
public data class AnnotationData(
    val name: String,
    val values: Map<String, Any?>
) {

    public fun getStringValue(key: String): String? = values[key]?.toString()

    public fun getIntValue(key: String): Int? = values[key] as? Int

    @Suppress("UNCHECKED_CAST")
    public fun getStringArrayValue(key: String): List<String>? = values[key] as? List<String>

    @Suppress("UNCHECKED_CAST")
    public fun getIntArrayValue(key: String): List<Int>? = values[key] as? List<Int>

    @Suppress("UNCHECKED_CAST")
    public fun getEnumValue(key: String): String? = values[key] as? String
}
