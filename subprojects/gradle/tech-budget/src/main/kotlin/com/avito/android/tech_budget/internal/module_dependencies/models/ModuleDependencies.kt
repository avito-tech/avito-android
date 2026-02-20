package com.avito.android.tech_budget.internal.module_dependencies.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class ModuleDependencies(
    @param:Json(name = "moduleName") val modulePath: String,
    @param:Json(name = "directImportedModulesCount") val directImportedModulesCount: Int,
    @param:Json(name = "directDependentModulesCount") val directDependentModulesCount: Int,
)
