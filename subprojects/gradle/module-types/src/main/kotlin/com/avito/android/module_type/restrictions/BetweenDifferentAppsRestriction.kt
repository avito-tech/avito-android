package com.avito.android.module_type.restrictions

import com.avito.android.module_type.ApplicationDeclaration
import com.avito.android.module_type.ModuleWithType
import com.avito.android.module_type.Severity
import com.avito.android.module_type.restrictions.exclusion.DependencyRestrictionExclusion
import com.avito.module.configurations.ConfigurationType

public class BetweenDifferentAppsRestriction(
    exclusions: List<DependencyRestrictionExclusion>,
    private val commonApp: ApplicationDeclaration,
    private val sharingApps: Set<ApplicationDeclaration> = emptySet(),
    override val reason: String,
    override val severity: Severity,
) : DependencyRestriction(exclusions) {

    override fun isRestrictedInternal(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType
    ): Boolean {
        if (dependency.type.app == commonApp) return false

        if (dependency.sharedBetweenApps) {
            return !module.sharedBetweenApps && module.type.app !in sharingApps
        }

        if (module.sharedBetweenApps) return true

        return module.type.app != dependency.type.app
    }
}
