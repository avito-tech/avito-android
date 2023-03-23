package com.avito.android.tech_budget.internal.module_dependencies.models

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class UploadModuleDependenciesRequest(
    @Json(name = "dumpInfo") val dumpInfo: DumpInfo,
    @Json(name = "moduleDependencies") val dependencies: Collection<ModuleDependencies>
)
