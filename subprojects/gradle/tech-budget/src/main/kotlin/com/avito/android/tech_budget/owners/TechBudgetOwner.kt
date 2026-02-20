package com.avito.android.tech_budget.owners

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class TechBudgetOwner(
    @param:Json(name = "teamID") public val teamID: String,
    @param:Json(name = "teamName") public val teamName: String,
    @param:Json(name = "unitID") public val unitID: String,
    @param:Json(name = "unitName") public val unitName: String,
)
