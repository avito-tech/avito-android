package com.avito.module.dependencies

import com.avito.module.configurations.ConfigurationType
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

public fun Project.directDependenciesOnProjects(
    types: Set<ConfigurationType>
): Map<ConfigurationType, Set<Project>> {
    return configurations
        .associateWith {
            it.dependencies
                .matching { it is ProjectDependency }
                .map { (it as ProjectDependency).dependencyProject }
                // project has dependency to itself in a default configuration
                .filter { it != this }
                .toSet()
        }
        .filterValues { it.isNotEmpty() }
        .mapKeys { ConfigurationType.of(it.key) }
        .filterKeys { type ->
            types.contains(type)
        }
}
