package com.avito.module.dependencies

import com.avito.module.configurations.ConfigurationType
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency

public fun Project.dependenciesOnProjects(
    types: Set<Class<out ConfigurationType>>
): Set<DefaultProjectDependency> {
    return configurations
        .associateWith {
            it.dependencies
                .withType(DefaultProjectDependency::class.java)
                // project has dependency to itself in a default configuration
                .filter { it.dependencyProject != this }
        }
        .filterValues { it.isNotEmpty() }
        .filterKeys { ConfigurationType.of(it).let { type -> types.any { it.isInstance(type) } } }
        .values
        .flatten()
        .toSet()
}
