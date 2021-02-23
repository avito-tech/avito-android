package com.avito.android.build_checks.internal.params

import org.funktionale.tries.Try

internal interface ParameterCheck {
    fun getMismatches(): Try<Collection<ParameterMismatch>>
}

internal class ParameterMismatch(
    val name: String,
    val expected: Any?,
    val actual: Any?
)
