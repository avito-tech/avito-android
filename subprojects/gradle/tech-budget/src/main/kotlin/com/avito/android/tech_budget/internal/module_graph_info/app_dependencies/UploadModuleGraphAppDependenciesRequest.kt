package com.avito.android.tech_budget.internal.module_graph_info.app_dependencies

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UploadModuleGraphAppDependenciesRequest(
    @Json(name = "dumpInfo") val dumpInfo: DumpInfo,
    @Json(name = "modulesToDemoApps") val modulesToDemoApps: List<ModuleDemoAppDependency>,
)

@JsonClass(generateAdapter = true)
internal data class ModuleDemoAppDependency(
    @Json(name = "module") val module: String,
    @Json(name = "demoApp") val demoApp: String,
)
