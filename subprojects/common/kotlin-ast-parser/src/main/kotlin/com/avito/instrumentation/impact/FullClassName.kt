package com.avito.instrumentation.impact

data class FullClassName(
    val packageName: String,
    val className: String
) {

    override fun toString(): String =
        if (packageName.isNotBlank()) {
            "$packageName.$className"
        } else {
            className
        }
}
