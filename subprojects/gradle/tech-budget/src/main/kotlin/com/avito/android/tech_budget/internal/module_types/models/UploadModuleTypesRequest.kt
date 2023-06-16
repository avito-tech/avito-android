package com.avito.android.tech_budget.internal.module_types.models

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class UploadModuleTypesRequest(
    @Json(name = "dumpInfo") val dumpInfo: DumpInfo,
    @Json(name = "modules") val modules: Collection<ModuleWithType>
)
