package com.avito.android.tech_budget.internal.warnings.log

import com.avito.android.model.Owner
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class ProjectInfo(
    val path: String,
    val owners: Collection<Owner>,
)
