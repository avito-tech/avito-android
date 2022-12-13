package com.avito.android.tech_budget.internal.ab_tests.models

import com.avito.android.tech_budget.ab_tests.ABTest
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class UploadABTestsRequest(
    val dumpInfo: DumpInfo,
    val abTests: Collection<ABTest>
)
