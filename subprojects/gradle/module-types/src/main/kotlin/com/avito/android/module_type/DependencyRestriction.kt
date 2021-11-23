package com.avito.android.module_type

import com.avito.module.configurations.ConfigurationType

public class DependencyRestriction(
    public val matcher: DependencyMatcher,
    public val exclusions: Set<DependencyMatcher> = emptySet(),
    public val description: String
)

internal fun DependencyRestriction.isViolated(
    from: ModuleWithType,
    to: ModuleWithType,
    configuration: ConfigurationType
): Boolean {
    val isExcluded = exclusions.any {
        it.match(from, to, configuration)
    }
    return !isExcluded && matcher.match(from, to, configuration)
}
