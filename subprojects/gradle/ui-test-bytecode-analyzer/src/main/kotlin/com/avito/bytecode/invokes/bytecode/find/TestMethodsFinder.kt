package com.avito.bytecode.invokes.bytecode.find

import com.avito.bytecode.invokes.bytecode.context.Context
import com.avito.bytecode.invokes.bytecode.model.FoundMethod
import com.avito.bytecode.invokes.test.isTest
import org.apache.bcel.classfile.JavaClass
import org.apache.bcel.classfile.Method

interface TestMethodsFinder {
    fun find(
        context: Context,
        classes: Iterable<JavaClass> = context.classes.values
    ): Set<FoundMethod>
}

class TestMethodsFinderImpl : TestMethodsFinder {
    private val result: MutableSet<FoundMethod> = mutableSetOf()

    override fun find(
        context: Context,
        classes: Iterable<JavaClass>
    ): Set<FoundMethod> {
        result.clear()
        classes.forEach { clazz ->
            visitJavaClass(context, clazz)
        }

        return result
    }

    private fun visitJavaClass(context: Context, javaClass: JavaClass) {
        if (!javaClass.isAbstract && !javaClass.isInterface) {
            context.getAllRealMethods(javaClass).forEach {
                visitJavaMethod(javaClass, it)
            }
        }
    }

    private fun visitJavaMethod(javaClass: JavaClass, method: Method) {
        if (method.isTest()) {
            result.add(
                FoundMethod.fromMethod(
                    javaClass = javaClass,
                    method = method
                )
            )
        }
    }
}
