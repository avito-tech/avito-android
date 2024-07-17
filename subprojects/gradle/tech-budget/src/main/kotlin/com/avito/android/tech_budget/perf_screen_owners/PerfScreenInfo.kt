package com.avito.android.tech_budget.perf_screen_owners

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class PerformanceScreenInfo(
    @Json(name = "contentTypes") public val contentTypes: List<PerformanceScreenContentTypes>,
    @Json(name = "description") public val description: String,
    @Json(name = "name") public val name: String,
    @Json(name = "owners") public val owners: List<String>
)

@JsonClass(generateAdapter = true)
public class PerformanceScreenContentTypes(
    @Json(name = "description") public val description: String,
    @Json(name = "isIndirect") public val isIndirect: Boolean = false,
    @Json(name = "name") public val name: String,
    @Json(name = "owners") public val owners: List<String>
)
