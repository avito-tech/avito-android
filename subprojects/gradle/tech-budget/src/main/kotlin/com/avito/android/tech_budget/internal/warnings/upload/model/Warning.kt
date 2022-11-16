package com.avito.android.tech_budget.internal.warnings.upload.model

import com.avito.android.model.Owner
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class Warning(
    @Json(name = "moduleName") val moduleName: String,
    @Json(name = "owners") val owners: Collection<Owner>,
    @Json(name = "sourceFile") val sourceFile: String?,
    @Json(name = "message") val fullMessage: String,
)
