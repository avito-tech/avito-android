package com.avito.android.tech_budget.internal.owners.models

import com.avito.android.model.Owner
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class UploadOwnersRequestBody(
    val dumpInfo: DumpInfo,
    val owners: Collection<Owner>
)
