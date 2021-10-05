package com.avito.module.dependencies

import com.avito.module.configurations.ConfigurationType
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

public fun Project.dependenciesOnProjects(
    types: Set<Class<out ConfigurationType>>
): Set<ProjectDependency> {
    return configurations
        .associateWith {
            it.dependencies
                .matching { it is ProjectDependency }
                .map { it as ProjectDependency }
                // project has dependency to itself in a default configuration
                .filter { it.dependencyProject != this }
        }
        .filterValues { it.isNotEmpty() }
        .filterKeys { ConfigurationType.of(it).let { type -> types.any { it.isInstance(type) } } }
        .values
        .flatten()
        .toSet()
}
