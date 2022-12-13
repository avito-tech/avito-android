package com.avito.android.tech_budget.internal.feature_toggles.models

import com.avito.android.tech_budget.feature_toggles.FeatureToggle
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class UploadFeatureTogglesRequest(
    val dumpInfo: DumpInfo,
    val featureToggles: Collection<FeatureToggle>
)
