package com.avito.android.tech_budget.deeplinks

import com.avito.android.model.Owner
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class DeepLink(
    @Json(name = "deepLinkName") public val className: String,
    @Json(name = "moduleName") public val moduleName: String,
    @Json(name = "path") public val path: String,
    @Json(name = "version") public val version: Int,
    @Json(name = "owners") public val owners: List<Owner>
)
