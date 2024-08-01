package com.avito.android.module_type.restrictions.exclusion

import com.avito.android.module_type.ModuleWithType
import com.avito.module.configurations.ConfigurationType

internal class BetweenModulesExclusion(
    private val fromModule: Set<String>,
    private val toDependency: Set<String>,
    override val reason: String,
) : DependencyRestrictionExclusion {

    override fun isExclusion(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType
    ): Boolean {
        return fromModule.contains(module.path) && toDependency.contains(dependency.path)
    }
}
