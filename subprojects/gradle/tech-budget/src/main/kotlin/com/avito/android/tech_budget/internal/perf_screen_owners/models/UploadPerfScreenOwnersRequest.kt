package com.avito.android.tech_budget.internal.perf_screen_owners.models

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.perf_screen_owners.PerformanceScreenInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class UploadPerfScreenOwnersRequest(
    @Json(name = "dumpInfo") val dumpInfo: DumpInfo,
    @Json(name = "screenInfos") val screenInfos: List<PerformanceScreenInfo>
)
