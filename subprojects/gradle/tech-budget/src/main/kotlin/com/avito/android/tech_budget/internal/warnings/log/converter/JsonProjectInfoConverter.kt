package com.avito.android.tech_budget.internal.warnings.log.converter

import com.avito.android.tech_budget.internal.di.MoshiProvider
import com.avito.android.tech_budget.internal.warnings.log.ProjectInfo

internal class JsonProjectInfoConverter(
    moshiProvider: MoshiProvider
) : ProjectInfoConverter {

    private val jsonAdapter by lazy { moshiProvider.provide().adapter(ProjectInfo::class.java) }

    override fun extractFromString(rawText: String): ProjectInfo {
        return requireNotNull(jsonAdapter.fromJson(rawText)) { "Log entry should not be null!" }
    }

    override fun convertToString(info: ProjectInfo): String {
        return jsonAdapter.toJson(info)
    }
}
