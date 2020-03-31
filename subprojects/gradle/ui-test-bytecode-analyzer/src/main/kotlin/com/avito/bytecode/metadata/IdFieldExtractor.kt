package com.avito.bytecode.metadata

import com.avito.bytecode.invokes.bytecode.context.Context
import org.apache.bcel.classfile.JavaClass

interface IdFieldExtractor {

    data class ScreenToId(val screenClass: String, val rootViewRId: String)

    fun extract(
        context: Context,
        targetClasses: Set<JavaClass>
    ): Set<ScreenToId>

    class Impl(private val fieldName: String, private val logger: (String) -> Unit) : IdFieldExtractor {

        override fun extract(
            context: Context,
            targetClasses: Set<JavaClass>
        ): Set<ScreenToId> {

            return targetClasses
                .mapNotNull { clazz ->
                    context.getAllRealFields(clazz)
                        .find { it.name == fieldName }
                        ?.let { field ->
                            try {
                                val constantValue = field.constantValue
                                if (constantValue == null) {
                                    logger.invoke("Can't get constant value $fieldName from class ${clazz.className}; field attributes are: ${field.attributes}")
                                }
                                clazz.className to constantValue?.toString()
                            } catch (t: Throwable) {
                                throw RuntimeException(
                                    "Failed to get field value: $fieldName from class: ${clazz.className}", t
                                )
                            }
                        }
                }
                .filter { it.second != null }
                .map { ScreenToId(screenClass = it.first, rootViewRId = it.second!!) }
                .toSet()
        }
    }
}

fun Set<IdFieldExtractor.ScreenToId>.toMap(): Map<String, Int> =
    map { it.screenClass to it.rootViewRId.toInt() }.toMap()
