package com.avito.android.tech_budget.internal.warnings.log.converter

import com.avito.android.tech_budget.internal.warnings.log.ProjectInfo
import com.squareup.moshi.JsonDataException

internal interface ProjectInfoConverter {

    @Throws(
        NullPointerException::class,
        JsonDataException::class
    )
    fun extractFromString(rawText: String): ProjectInfo

    fun convertToString(info: ProjectInfo): String

    companion object {
        fun default(): ProjectInfoConverter = JsonProjectInfoConverter()
    }
}
