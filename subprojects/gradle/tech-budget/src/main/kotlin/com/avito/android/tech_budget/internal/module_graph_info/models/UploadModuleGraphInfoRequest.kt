package com.avito.android.tech_budget.internal.module_graph_info.models

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UploadModuleGraphInfoRequest(
    @Json(name = "dumpInfo") val dumpInfo: DumpInfo,
    @Json(name = "dependencies") val dependencies: List<Dependency>,
    @Json(name = "sizes") val sizes: Map<String, Int>,
    @Json(name = "modulesToDemoApps") val modulesToDemoApps: List<ModuleDemoAppDependency>,
)

@JsonClass(generateAdapter = true)
internal data class Dependency(
    @Json(name = "from") val from: String,
    @Json(name = "to") val to: String,
    @Json(name = "type") val type: String,
)

@JsonClass(generateAdapter = true)
internal data class ModuleDemoAppDependency(
    @Json(name = "module") val module: String,
    @Json(name = "demoApp") val demoApp: String,
)
