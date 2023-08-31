package com.avito.android.module_type.validation.configurations.missings.implementations.internal

import com.avito.android.module_type.FunctionalType

internal data class ProjectConvertedData(
    val modulePath: String,
    val logicalModule: String,
    val functionalType: FunctionalType?,
    val level: Int,
)
