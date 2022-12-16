package com.avito.android.tech_budget.feature_toggles

import com.avito.android.model.Owner
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class FeatureToggle(
    @Json(name = "key") public val key: String,
    @Json(name = "defaultValue") public val defaultValue: String,
    @Json(name = "description") public val description: String,
    @Json(name = "isRemote") public val isRemote: Boolean,
    @Json(name = "owners") public val owners: List<Owner>
)
