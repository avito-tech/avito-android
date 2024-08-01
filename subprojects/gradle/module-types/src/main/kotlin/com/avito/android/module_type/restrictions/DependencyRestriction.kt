package com.avito.android.module_type.restrictions

import com.avito.android.module_type.ModuleWithType
import com.avito.android.module_type.Severity
import com.avito.android.module_type.restrictions.exclusion.DependencyRestrictionExclusion
import com.avito.module.configurations.ConfigurationType

public abstract class DependencyRestriction(
    private val exclusions: List<DependencyRestrictionExclusion>
) {

    public abstract val reason: String
    public abstract val severity: Severity

    public fun isRestricted(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType,
    ): Boolean {
        return if (exclusions.any { it.isExclusion(module, dependency, configuration) }) {
            false
        } else {
            isRestrictedInternal(module, dependency, configuration)
        }
    }

    protected abstract fun isRestrictedInternal(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType
    ): Boolean
}
