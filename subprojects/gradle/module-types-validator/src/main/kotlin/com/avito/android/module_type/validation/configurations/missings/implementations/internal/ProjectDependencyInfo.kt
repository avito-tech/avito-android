package com.avito.android.module_type.validation.configurations.missings.implementations.internal

import com.avito.android.module_type.FunctionalType

internal data class ProjectDependencyInfo(
    val modulePath: String,
    val fullPath: String,
    val logicalModule: String,
    val functionalType: FunctionalType?,
)
