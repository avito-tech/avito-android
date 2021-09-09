package com.avito.android.lint.internal

import com.avito.capitalize

private val noSymbols = Regex("[\\W]+")

internal fun String.validInGradleTaskName(): String = split(noSymbols)
    .joinToString(separator = "") { it.capitalize() }
