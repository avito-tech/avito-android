package com.avito.android.module_type.validation.publicimpl.internal

import com.avito.android.module_type.FunctionalType
import java.io.Serializable

internal data class ProjectDependencyInfo(
    val modulePath: String,
    val fullPath: String,
    val level: Int,
    val logicalModule: String,
    val functionalType: FunctionalType?,
) : Serializable
