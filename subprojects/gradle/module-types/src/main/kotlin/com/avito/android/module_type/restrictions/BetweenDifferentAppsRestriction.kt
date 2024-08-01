package com.avito.android.module_type.restrictions

import com.avito.android.module_type.ApplicationDeclaration
import com.avito.android.module_type.ModuleWithType
import com.avito.android.module_type.Severity
import com.avito.android.module_type.restrictions.exclusion.DependencyRestrictionExclusion
import com.avito.module.configurations.ConfigurationType

public class BetweenDifferentAppsRestriction(
    exclusions: List<DependencyRestrictionExclusion>,
    private val commonApp: ApplicationDeclaration,
    override val reason: String,
    override val severity: Severity,
) : DependencyRestriction(exclusions) {

    override fun isRestrictedInternal(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType
    ): Boolean {
        return module.type.app != dependency.type.app
            && dependency.type.app != commonApp
    }
}
