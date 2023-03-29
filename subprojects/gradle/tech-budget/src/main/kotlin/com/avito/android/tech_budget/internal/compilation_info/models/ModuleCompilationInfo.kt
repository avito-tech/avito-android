package com.avito.android.tech_budget.internal.compilation_info.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class ModuleCompilationInfo(
    @Json(name = "moduleName") val modulePath: String,
    @Json(name = "durationMs") val timeMs: Long,
)
