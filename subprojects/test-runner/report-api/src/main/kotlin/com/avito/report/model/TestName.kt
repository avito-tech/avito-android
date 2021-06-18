package com.avito.report.model

public data class TestName(val className: String, val methodName: String) {

    val packageName: String = className.substringBeforeLast(delimiter)

    val name: String = "$className.$methodName"

    override fun toString(): String = name

    public companion object {
        public const val delimiter: Char = '.'
    }
}
