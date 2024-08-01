package com.avito.android.module_type.restrictions

import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleWithType
import com.avito.android.module_type.Severity
import com.avito.android.module_type.restrictions.exclusion.DependencyRestrictionExclusion
import com.avito.module.configurations.ConfigurationType

public class BetweenFunctionalTypesRestriction(
    private val fromType: FunctionalType,
    private val allowedTypesByConfiguration: Map<ConfigurationType, Set<FunctionalType>>,
    override val reason: String,
    override val severity: Severity,
    exclusions: List<DependencyRestrictionExclusion>,
) : DependencyRestriction(exclusions) {

    override fun isRestrictedInternal(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType
    ): Boolean {
        return if (module.type.type == fromType) {
            val allowed = allowedTypesByConfiguration[configuration]
            allowed == null || !allowed.contains(dependency.type.type)
        } else {
            false
        }
    }
}
