package com.avito.bytecode.invokes.bytecode.tracer

import com.avito.bytecode.invokes.bytecode.context.Context
import com.avito.bytecode.invokes.model.Invoke
import org.apache.bcel.classfile.JavaClass
import org.apache.bcel.classfile.Method

typealias InvokesListener = (invoke: Invoke) -> Unit

interface InvokesTracer {
    fun trace(context: Context, listener: InvokesListener)
}

class InvokesTracerImpl : InvokesTracer {

    override fun trace(context: Context, listener: InvokesListener) {
        context.classes.values.parallelStream()
            .forEach { clazz ->
                val methodInvokesTracer = TestAwareMethodInvokesTracer(context)
                parseClass(
                    context = context,
                    methodInvokesTracer = methodInvokesTracer,
                    clazz = clazz,
                    listener = listener
                )
            }
    }

    private fun parseClass(
        context: Context,
        methodInvokesTracer: MethodInvokesTracer,
        clazz: JavaClass,
        listener: InvokesListener
    ) {
        if (clazz.isInterface || clazz.isAbstract) {
            return
        }

        visitJavaClass(
            context = context,
            methodInvokesTracer = methodInvokesTracer,
            javaClass = clazz,
            listener = listener
        )
    }

    private fun visitJavaClass(
        context: Context,
        methodInvokesTracer: MethodInvokesTracer,
        javaClass: JavaClass,
        listener: InvokesListener
    ) {
        context.getAllRealMethods(javaClass).forEach {
            visitMethod(
                methodInvokesTracer = methodInvokesTracer,
                javaClass = javaClass,
                method = it,
                listener = listener
            )
        }
    }

    private fun visitMethod(
        methodInvokesTracer: MethodInvokesTracer,
        javaClass: JavaClass,
        method: Method,
        listener: InvokesListener
    ) {
        methodInvokesTracer.trace(
            method = method,
            clazz = javaClass,
            listener = listener
        )
    }
}
