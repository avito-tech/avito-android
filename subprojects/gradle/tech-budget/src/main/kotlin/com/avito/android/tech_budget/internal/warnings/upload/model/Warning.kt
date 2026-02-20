package com.avito.android.tech_budget.internal.warnings.upload.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class Warning(
    @param:Json(name = "location") val location: String,
    @param:Json(name = "moduleName") val moduleName: String,
    @param:Json(name = "message") val message: String,
    @param:Json(name = "groupID") val groupID: String,
    @param:Json(name = "ruleID") val ruleID: String,
    @param:Json(name = "debt") val debt: Int,
)
