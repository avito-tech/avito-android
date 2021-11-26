package com.avito.android.module_type

import com.avito.module.configurations.ConfigurationType

public interface DependencyMatcher {

    @Deprecated(
        "Use matches function instead",
        ReplaceWith("matches")
    )
    public fun match(from: ModuleWithType, to: ModuleWithType, configuration: ConfigurationType): Boolean =
        false

    /**
     * @param module current module which has another module as `dependency`
     * @param dependency dependent module
     */
    @Suppress("DEPRECATION")
    public fun matches(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType
    ): Boolean = match(from = module, to = dependency, configuration)

    public fun description(): String
}
