package com.avito.android.tech_budget.ab_tests

import com.avito.android.model.Owner
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class ABTest(
    @param:Json(name = "key") public val key: String,
    @param:Json(name = "defaultGroup") public val defaultGroup: String,
    @param:Json(name = "groups") public val groups: List<String>,
    @param:Json(name = "owners") public val owners: List<Owner>
)
