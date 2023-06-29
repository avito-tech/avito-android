package com.avito.android.tech_budget.internal.warnings.upload.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class Warning(
    @Json(name = "location") val location: String,
    @Json(name = "moduleName") val moduleName: String,
    @Json(name = "message") val message: String,
    @Json(name = "groupID") val groupID: String,
    @Json(name = "ruleID") val ruleID: String,
    @Json(name = "debt") val debt: Int,
)
