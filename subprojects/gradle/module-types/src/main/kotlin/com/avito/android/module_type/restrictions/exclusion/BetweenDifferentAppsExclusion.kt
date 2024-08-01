package com.avito.android.module_type.restrictions.exclusion

import com.avito.android.module_type.ApplicationDeclaration
import com.avito.android.module_type.ModuleWithType
import com.avito.module.configurations.ConfigurationType

internal class BetweenDifferentAppsExclusion(
    override val reason: String,
    private val moduleApp: ApplicationDeclaration,
    private val dependencies: Set<String>,
) : DependencyRestrictionExclusion {

    override fun isExclusion(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType
    ): Boolean {
        return if (module.type.app == moduleApp) {
            dependencies.contains(dependency.path)
        } else {
            false
        }
    }
}
