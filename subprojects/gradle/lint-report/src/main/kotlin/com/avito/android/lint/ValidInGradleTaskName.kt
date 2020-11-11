package com.avito.android.lint

private val noSymbols = Regex("[\\W]+")

internal fun String.validInGradleTaskName(): String = split(noSymbols)
    .joinToString(separator = "") { it.capitalize() }
