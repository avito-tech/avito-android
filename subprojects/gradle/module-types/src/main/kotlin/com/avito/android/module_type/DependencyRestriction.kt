package com.avito.android.module_type

import com.avito.module.configurations.ConfigurationType

public class DependencyRestriction(
    public val matcher: DependencyMatcher,
    public val exclusions: Set<DependencyMatcher> = emptySet(),
    @Deprecated("Use matcher's description instead. This property is not used and will be deleted.")
    public val description: String = ""
)

internal fun DependencyRestriction.isViolated(
    module: ModuleWithType,
    dependency: ModuleWithType,
    configuration: ConfigurationType
): Boolean {
    val isExcluded = exclusions.any {
        it.matches(module, dependency, configuration)
    }
    return !isExcluded && matcher.matches(module, dependency, configuration)
}
