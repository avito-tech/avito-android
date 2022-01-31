package com.avito.android.internal

import com.avito.android.AnnotationData
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
 * The default annotation values parsing is broken
 * implementation details https://source.android.com/devices/tech/dalvik/dex-format#dalvik-annotation-default
 */
internal object AnnotationExtractor {

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
