package com.avito.android.tech_budget.feature_toggles

import com.avito.android.model.Owner
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class FeatureToggle(
    @param:Json(name = "key") public val key: String,
    @param:Json(name = "defaultValue") public val defaultValue: String,
    @param:Json(name = "description") public val description: String,
    @param:Json(name = "isRemote") public val isRemote: Boolean,
    @param:Json(name = "owners") public val owners: List<Owner>
)
