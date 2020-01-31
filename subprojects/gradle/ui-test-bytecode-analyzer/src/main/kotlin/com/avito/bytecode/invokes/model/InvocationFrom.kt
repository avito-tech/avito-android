package com.avito.bytecode.invokes.model

import com.avito.bytecode.invokes.test.isTest
import org.apache.bcel.classfile.JavaClass
import org.apache.bcel.classfile.Method

data class InvocationFrom(
    val method: Method,
    val clazz: JavaClass
) {
    val isTest: Boolean
        get() = method.isTest()
}
