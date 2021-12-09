package com.avito.android.module_type

import com.avito.module.configurations.ConfigurationType

public interface DependencyMatcher {

    /**
     * @param module current module which has another module as `dependency`
     * @param dependency dependent module
     */
    @Suppress("DEPRECATION")
    public fun matches(
        module: ModuleWithType,
        dependency: ModuleWithType,
        configuration: ConfigurationType
    ): Boolean

    public fun description(): String
}
