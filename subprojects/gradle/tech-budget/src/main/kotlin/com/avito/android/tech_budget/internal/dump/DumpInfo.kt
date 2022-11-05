package com.avito.android.tech_budget.internal.dump

import com.avito.android.tech_budget.DumpInfoConfiguration
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class DumpInfo(
    @Json(name = "commitHash") val commitHash: String,
    @Json(name = "commitDate") val commitDate: String,
    @Json(name = "platform") val platform: String,
    @Json(name = "project") val project: String
) {

    companion object {
        fun fromExtension(extension: DumpInfoConfiguration) = DumpInfo(
            extension.commitHash.get(),
            extension.currentDate.get(),
            extension.platform.get(),
            extension.project.get(),
        )
    }
}
