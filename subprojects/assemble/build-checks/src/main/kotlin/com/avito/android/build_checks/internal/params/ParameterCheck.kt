package com.avito.android.build_checks.internal.params

import com.avito.android.Result

internal interface ParameterCheck {

    fun getMismatches(): Result<Collection<ParameterMismatch>>
}

internal class ParameterMismatch(
    val name: String,
    val expected: Any?,
    val actual: Any?
)
