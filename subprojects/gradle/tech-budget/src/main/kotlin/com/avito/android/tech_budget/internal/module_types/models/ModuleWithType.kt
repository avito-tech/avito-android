package com.avito.android.tech_budget.internal.module_types.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class ModuleWithType(
    @param:Json(name = "moduleName") val moduleName: String,
    @param:Json(name = "functionalType") val functionalType: String,
)
