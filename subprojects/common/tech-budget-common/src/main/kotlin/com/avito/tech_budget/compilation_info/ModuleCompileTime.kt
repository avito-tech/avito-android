package com.avito.tech_budget.compilation_info

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class ModuleCompileTime(
    val modulePath: String,
    val compileTimeMs: Long,
)
