package com.avito.android.lint.internal

private val noSymbols = Regex("[\\W]+")

internal fun String.validInGradleTaskName(): String = split(noSymbols)
    .joinToString(separator = "") { it.capitalize() }
