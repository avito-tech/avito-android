package com.avito.android.module_type

import com.avito.module.configurations.ConfigurationType

public class DependencyRestriction(
    public val matcher: DependencyMatcher,
    public val exclusions: Set<DependencyMatcher> = emptySet(),
) {

    internal fun isViolated(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType
    ): Boolean {
        val isExcluded = exclusions.any {
            it.matches(module, dependency, configuration)
        }
        return !isExcluded && matcher.matches(module, dependency, configuration)
    }
}
