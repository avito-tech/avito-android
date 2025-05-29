package com.avito.android.tech_budget.internal.module_graph_info.sizes

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UploadModuleSizesRequest(
    @Json(name = "dumpInfo") val dumpInfo: DumpInfo,
    @Json(name = "sizes") val sizes: Map<String, Int>,
)
