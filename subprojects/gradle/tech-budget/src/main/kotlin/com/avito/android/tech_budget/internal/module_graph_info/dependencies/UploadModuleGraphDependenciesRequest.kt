package com.avito.android.tech_budget.internal.module_graph_info.dependencies

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UploadModuleGraphDependenciesRequest(
    @param:Json(name = "dumpInfo") val dumpInfo: DumpInfo,
    @param:Json(name = "dependencies") val dependencies: List<Dependency>,
)

@JsonClass(generateAdapter = true)
internal data class Dependency(
    @param:Json(name = "from") val from: String,
    @param:Json(name = "to") val to: String,
    @param:Json(name = "type") val type: String,
)
