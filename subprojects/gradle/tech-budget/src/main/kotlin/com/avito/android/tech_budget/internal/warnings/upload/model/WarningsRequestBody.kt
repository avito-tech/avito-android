package com.avito.android.tech_budget.internal.warnings.upload.model

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class WarningsRequestBody(
    val dumpInfo: DumpInfo,
    val warnings: List<Warning>
)
