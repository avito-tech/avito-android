package com.avito.android.plugin.build_param_check

import org.funktionale.tries.Try

internal interface Check {
    fun getMismatches(): Try<Collection<Mismatch>>
}

internal class Mismatch(
    val name: String,
    val expected: Any?,
    val actual: Any?
)
