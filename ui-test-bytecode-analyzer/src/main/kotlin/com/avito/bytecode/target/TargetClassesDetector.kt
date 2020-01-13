package com.avito.bytecode.target

import com.avito.bytecode.invokes.bytecode.context.Context
import org.apache.bcel.classfile.JavaClass

sealed class TargetClassesDetector {

    abstract fun detect(
        context: Context,
        clazz: JavaClass
    ): Boolean

    class RegexTargetClassesDetector(
        private val regex: Regex
    ) : TargetClassesDetector() {

        override fun detect(
            context: Context,
            clazz: JavaClass
        ): Boolean = regex.containsMatchIn(clazz.className)
    }

    class InterfaceBasedDetector(
        private val interfaceName: String
    ) : TargetClassesDetector() {

        override fun detect(
            context: Context,
            clazz: JavaClass
        ): Boolean = context.implementationOf(clazz).find {
            it == interfaceName
        } != null
    }

    class RegexFileNameDetector(
        private val regex: List<Regex>
    ) : TargetClassesDetector() {

        override fun detect(
            context: Context,
            clazz: JavaClass
        ): Boolean = regex.any { it.containsMatchIn(clazz.fileName) }
    }
}
