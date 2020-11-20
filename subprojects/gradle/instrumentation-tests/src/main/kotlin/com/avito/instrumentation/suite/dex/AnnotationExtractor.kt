package com.avito.instrumentation.suite.dex

import org.jf.dexlib2.ValueType
import org.jf.dexlib2.iface.Annotation
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.value.ArrayEncodedValue
import org.jf.dexlib2.iface.value.EncodedValue
import org.jf.dexlib2.iface.value.EnumEncodedValue
import org.jf.dexlib2.iface.value.IntEncodedValue
import org.jf.dexlib2.iface.value.LongEncodedValue
import org.jf.dexlib2.iface.value.StringEncodedValue

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

/**
 * @param type пример: "Lorg/junit/Test;"
 */
class AnnotationType(val type: String) {

    init {
        require(type.endsWith(';')) { "Invalid type definition: $type" }
    }
}

object AnnotationExtractor {

    fun toAnnotationData(annotation: Annotation): AnnotationData {
        return AnnotationData(
            name = annotation.type.fromDexType(),
            values = annotation.elements.map { it.name to it.value.decode() }.toMap()
        )
    }

    private fun EncodedValue.decode(): Any? {
        return when (this.valueType) {
            ValueType.INT -> (this as IntEncodedValue).value
            ValueType.LONG -> (this as LongEncodedValue).value
            ValueType.STRING -> (this as StringEncodedValue).value
            ValueType.ENUM -> (this as EnumEncodedValue).value.name
            ValueType.ARRAY -> (this as ArrayEncodedValue).value.map { it.decode() }
            else -> null
        }
    }

    private fun String.fromDexType(): String {
        require(first() == DEX_OBJECT_TYPE_PREFIX && last() == ';') { "not a dex type: $this" }

        return drop(1)
            .dropLast(1)
            .replace('/', '.')
    }

    fun hasAnnotation(method: Method, annotationType: AnnotationType): Boolean {
        return method.annotations.any { it.type == annotationType.type }
    }
}

private const val DEX_OBJECT_TYPE_PREFIX = 'L'
