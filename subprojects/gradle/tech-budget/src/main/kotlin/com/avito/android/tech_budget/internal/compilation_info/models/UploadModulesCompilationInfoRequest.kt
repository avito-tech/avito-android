package com.avito.android.tech_budget.internal.compilation_info.models

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class UploadModulesCompilationInfoRequest(
    @Json(name = "dumpInfo") val dumpInfo: DumpInfo,
    @Json(name = "moduleCompilations") val compilationInfo: Collection<ModuleCompilationInfo>
)
