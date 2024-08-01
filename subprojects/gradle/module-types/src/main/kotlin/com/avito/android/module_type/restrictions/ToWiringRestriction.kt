package com.avito.android.module_type.restrictions

import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleWithType
import com.avito.android.module_type.Severity
import com.avito.android.module_type.restrictions.exclusion.DependencyRestrictionExclusion
import com.avito.module.configurations.ConfigurationType

public class ToWiringRestriction(
    exclusions: List<DependencyRestrictionExclusion>,
    override val reason: String,
    override val severity: Severity,
) : DependencyRestriction(exclusions) {

    override fun isRestrictedInternal(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType
    ): Boolean {
        fun String.logicalModule() = this.substringBeforeLast(':')
        val moduleType = module.type.type
        val dependencyType = dependency.type.type
        return when {
            moduleType != FunctionalType.ImplWiring && moduleType != FunctionalType.FakeWiring -> false
            moduleType == FunctionalType.ImplWiring && dependencyType != FunctionalType.Impl -> true
            moduleType == FunctionalType.FakeWiring && dependencyType != FunctionalType.Fake -> true
            module.path.logicalModule() != dependency.path.logicalModule() -> true
            else -> false
        }
    }
}
