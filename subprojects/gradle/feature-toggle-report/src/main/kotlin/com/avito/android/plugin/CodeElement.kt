package com.avito.android.plugin

import java.time.LocalDate

internal data class CodeElement(
    val codeLine: String,
    val changeTime: LocalDate,
    val email: String
)
