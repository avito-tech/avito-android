package com.avito.android.tech_budget.deeplinks

import com.avito.android.model.Owner
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class DeepLink(
    @param:Json(name = "deepLinkName") public val className: String,
    @param:Json(name = "moduleName") public val moduleName: String,
    @param:Json(name = "path") public val path: String,
    @param:Json(name = "version") public val version: Int,
    @param:Json(name = "owners") public val owners: List<Owner>
)
