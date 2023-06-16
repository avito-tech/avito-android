package com.avito.android.tech_budget.internal.module_types.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class ModuleWithType(
    @Json(name = "moduleName") val moduleName: String,
    @Json(name = "functionalType") val functionalType: String,
)
