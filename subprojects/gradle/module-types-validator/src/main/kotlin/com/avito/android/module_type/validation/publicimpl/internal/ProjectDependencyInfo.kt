package com.avito.android.module_type.validation.publicimpl.internal

import com.avito.android.module_type.FunctionalType
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
internal data class ProjectDependencyInfo(
    val modulePath: String,
    val fullPath: String,
    val level: Int,
    val logicalModule: String,
    val functionalType: FunctionalType?,
) : Serializable

internal fun ProjectDependencyInfo.isPublicType(): Boolean {
    return functionalType == FunctionalType.Public
}
