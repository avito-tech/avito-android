package com.avito.instrumentation.impact

public data class FullClassName(
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
