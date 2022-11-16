package com.avito.android.tech_budget.internal.warnings.log.converter

import com.avito.android.OwnerSerializer
import com.avito.android.tech_budget.internal.di.MoshiProvider
import com.avito.android.tech_budget.internal.warnings.log.ProjectInfo
import com.squareup.moshi.JsonDataException
import javax.inject.Provider

internal interface ProjectInfoConverter {

    @Throws(
        NullPointerException::class,
        JsonDataException::class
    )
    fun extractFromString(rawText: String): ProjectInfo

    fun convertToString(info: ProjectInfo): String

    companion object {
        fun default(ownerSerializer: Provider<OwnerSerializer>): ProjectInfoConverter =
            JsonProjectInfoConverter(MoshiProvider(ownerSerializer))
    }
}
