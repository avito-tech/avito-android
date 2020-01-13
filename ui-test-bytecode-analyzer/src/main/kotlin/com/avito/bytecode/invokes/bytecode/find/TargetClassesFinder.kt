package com.avito.bytecode.invokes.bytecode.find

import com.avito.bytecode.invokes.bytecode.context.Context
import com.avito.bytecode.target.TargetClassesDetector
import org.apache.bcel.classfile.JavaClass

interface TargetClassesFinder {
    fun find(context: Context): Set<JavaClass>
}

class TargetClassesFinderImpl(
    private val targetClassesDetector: TargetClassesDetector
) : TargetClassesFinder {

    private val result: MutableSet<JavaClass> = mutableSetOf()

    override fun find(context: Context): Set<JavaClass> {
        result.clear()
        context.classes.forEach { _, clazz ->
            visitJavaClass(context, clazz)
        }

        return result
    }

    private fun visitJavaClass(context: Context, javaClass: JavaClass) {
        if (targetClassesDetector.detect(context, javaClass) && !javaClass.isInterface && !javaClass.isAbstract) {
            result.add(javaClass)
        }
    }
}
