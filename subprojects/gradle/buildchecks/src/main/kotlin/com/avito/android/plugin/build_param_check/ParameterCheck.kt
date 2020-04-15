package com.avito.android.plugin.build_param_check

import org.funktionale.tries.Try

internal interface ParameterCheck {
    fun getMismatches(): Try<Collection<ParameterMismatch>>
}

internal class ParameterMismatch(
    val name: String,
    val expected: Any?,
    val actual: Any?
)
