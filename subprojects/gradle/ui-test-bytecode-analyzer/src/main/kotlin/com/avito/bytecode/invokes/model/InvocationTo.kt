package com.avito.bytecode.invokes.model

data class InvocationTo(
    val className: String,
    val methodName: String,
    val methodNameWithArguments: String
) {
    val fullName: String
        get() = "$className.$methodNameWithArguments"
}
