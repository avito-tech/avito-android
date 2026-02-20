package com.avito.android.tech_budget.internal.module_types.models

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class UploadModuleTypesRequest(
    @param:Json(name = "dumpInfo") val dumpInfo: DumpInfo,
    @param:Json(name = "modules") val modules: Collection<ModuleWithType>
)
