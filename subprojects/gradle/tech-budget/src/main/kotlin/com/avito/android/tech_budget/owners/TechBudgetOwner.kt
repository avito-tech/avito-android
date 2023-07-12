package com.avito.android.tech_budget.owners

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class TechBudgetOwner(
    @Json(name = "teamID") public val teamID: String,
    @Json(name = "teamName") public val teamName: String,
    @Json(name = "unitID") public val unitID: String,
    @Json(name = "unitName") public val unitName: String,
)
