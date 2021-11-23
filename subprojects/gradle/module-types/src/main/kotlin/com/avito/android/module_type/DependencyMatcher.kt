package com.avito.android.module_type

import com.avito.module.configurations.ConfigurationType

public interface DependencyMatcher {

    public fun match(from: ModuleWithType, to: ModuleWithType, configuration: ConfigurationType): Boolean

    public fun description(): String
}
