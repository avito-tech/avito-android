package com.avito.android.tech_budget.internal.module_dependencies.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class ModuleDependencies(
    @Json(name = "moduleName") val modulePath: String,
    @Json(name = "directImportedModulesCount") val directImportedModulesCount: Int,
    @Json(name = "directDependentModulesCount") val directDependentModulesCount: Int,
)
