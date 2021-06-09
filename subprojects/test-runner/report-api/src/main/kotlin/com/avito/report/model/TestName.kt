package com.avito.report.model

public data class TestName(val className: String, val methodName: String) {

    val packageName: String = className.substringBeforeLast(delimiter)

    val name: String = "$className.$methodName"

    public constructor(name: String) : this(
        className = name.substringBeforeLast(delimiter),
        methodName = name.substringAfterLast(delimiter)
    )

    override fun toString(): String = name

    public companion object {
        public const val delimiter: Char = '.'
    }
}
