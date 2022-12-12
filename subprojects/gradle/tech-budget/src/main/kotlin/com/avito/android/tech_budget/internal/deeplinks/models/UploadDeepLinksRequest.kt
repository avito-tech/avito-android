package com.avito.android.tech_budget.internal.deeplinks.models

import com.avito.android.tech_budget.deeplinks.DeepLink
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class UploadDeepLinksRequest(
    val dumpInfo: DumpInfo,
    val deepLinks: Collection<DeepLink>
)
