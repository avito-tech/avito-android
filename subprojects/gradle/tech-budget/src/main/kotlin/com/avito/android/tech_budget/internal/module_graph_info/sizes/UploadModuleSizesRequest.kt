package com.avito.android.tech_budget.internal.module_graph_info.sizes

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UploadModuleSizesRequest(
    @param:Json(name = "dumpInfo") val dumpInfo: DumpInfo,
    @param:Json(name = "sizes") val sizes: Map<String, Int>,
)
