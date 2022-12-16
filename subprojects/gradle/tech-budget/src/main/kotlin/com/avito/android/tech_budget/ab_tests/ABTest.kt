package com.avito.android.tech_budget.ab_tests

import com.avito.android.model.Owner
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class ABTest(
    @Json(name = "key") public val key: String,
    @Json(name = "defaultGroup") public val defaultGroup: String,
    @Json(name = "groups") public val groups: List<String>,
    @Json(name = "owners") public val owners: List<Owner>
)
