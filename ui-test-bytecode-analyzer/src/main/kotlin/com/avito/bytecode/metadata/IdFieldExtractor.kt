package com.avito.bytecode.metadata

import com.avito.bytecode.invokes.bytecode.context.Context
import org.apache.bcel.classfile.JavaClass
import java.lang.RuntimeException

interface IdFieldExtractor {

    data class ScreenToId(val screenClass: String, val rootViewRId: String)

    fun extract(
        context: Context,
        targetClasses: Set<JavaClass>
    ): Set<ScreenToId>

    class Impl(private val fieldName: String) : IdFieldExtractor {

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
                                ScreenToId(
                                    screenClass = clazz.className,
                                    rootViewRId = field.constantValue.toString()
                                )
                            } catch (t: Throwable) {
                                throw RuntimeException(
                                    "Failed to get field value: $fieldName from class: ${clazz.className}", t
                                )
                            }
                        }
                }
                .toSet()
        }
    }
}

fun Set<IdFieldExtractor.ScreenToId>.toMap(): Map<String, Int> = map { it.screenClass to it.rootViewRId.toInt() }.toMap()
