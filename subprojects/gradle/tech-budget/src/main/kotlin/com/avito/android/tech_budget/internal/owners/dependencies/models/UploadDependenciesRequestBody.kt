package com.avito.android.tech_budget.internal.owners.dependencies.models

import com.avito.android.owner.dependency.OwnedDependency
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class UploadDependenciesRequestBody(
    @param:Json(name = "dumpInfo") val dumpInfo: DumpInfo,
    @param:Json(name = "modules") val modules: Collection<OwnedDependency>
)
