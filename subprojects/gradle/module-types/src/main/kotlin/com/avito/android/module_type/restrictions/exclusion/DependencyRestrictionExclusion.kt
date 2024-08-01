package com.avito.android.module_type.restrictions.exclusion

import com.avito.android.module_type.ModuleWithType
import com.avito.module.configurations.ConfigurationType

public interface DependencyRestrictionExclusion {

    public val reason: String

    public fun isExclusion(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType,
    ): Boolean
}
