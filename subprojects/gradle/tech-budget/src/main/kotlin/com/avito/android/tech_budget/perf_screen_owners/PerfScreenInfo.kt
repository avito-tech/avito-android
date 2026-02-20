package com.avito.android.tech_budget.perf_screen_owners

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class PerformanceScreenInfo(
    @param:Json(name = "contentTypes") public val contentTypes: List<PerformanceScreenContentTypes>,
    @param:Json(name = "description") public val description: String,
    @param:Json(name = "name") public val name: String,
    @param:Json(name = "owners") public val owners: List<String>
)

@JsonClass(generateAdapter = true)
public class PerformanceScreenContentTypes(
    @param:Json(name = "description") public val description: String,
    @param:Json(name = "isIndirect") public val isIndirect: Boolean = false,
    @param:Json(name = "name") public val name: String,
    @param:Json(name = "owners") public val owners: List<String>
)
