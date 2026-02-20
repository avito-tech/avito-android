package com.avito.android.tech_budget.internal.module_dependencies.models

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class UploadModuleDependenciesRequest(
    @param:Json(name = "dumpInfo") val dumpInfo: DumpInfo,
    @param:Json(name = "moduleDependencies") val dependencies: Collection<ModuleDependencies>
)
