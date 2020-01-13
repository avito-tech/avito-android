package com.avito.bytecode.invokes.bytecode.model

import com.avito.bytecode.invokes.bytecode.fullName
import org.apache.bcel.classfile.JavaClass
import org.apache.bcel.classfile.Method

data class FoundMethod(
    val className: String,
    val methodName: String,
    val methodNameWithArguments: String,
    val name: String = "$className.$methodName"
) {
    override fun toString(): String = "$className.$methodNameWithArguments"

    companion object {
        fun fromMethod(javaClass: JavaClass, method: Method) = FoundMethod(
            className = javaClass.className,
            methodName = method.name,
            methodNameWithArguments = method.fullName
        )
    }
}
