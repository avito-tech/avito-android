package com.avito.bytecode.metadata

import com.avito.bytecode.invokes.bytecode.context.Context
import org.apache.bcel.classfile.JavaClass

interface ModulePathExtractor {

    fun extract(
        context: Context,
        targetClasses: Set<JavaClass>
    ): Set<ScreenToModulePath>

    class Impl(private val fieldName: String) : ModulePathExtractor {

        override fun extract(
            context: Context,
            targetClasses: Set<JavaClass>
        ): Set<ScreenToModulePath> {
            return targetClasses.mapNotNull { clazz ->
                context.getAllRealFields(clazz)
                    .find { it.name == fieldName }
                    ?.let { field ->
                        val constantValue = field.constantValue
                        val string = constantValue!!.toString()
                        val path = string.substring(1, string.length - 1)
                        ScreenToModulePath(
                            screenClass = clazz.className,
                            modulePath = ModulePath(path)
                        )
                    }
            }.toSet()
        }
    }
}
