package com.avito.android.module_type.internal

import com.avito.android.module_type.ModuleWithType
import com.avito.module.configurations.ConfigurationType
import java.io.Serializable

internal data class ModuleDescription(
    val module: ModuleWithType,
    val directDependencies: Map<ConfigurationType, Set<String>>
) : Serializable
