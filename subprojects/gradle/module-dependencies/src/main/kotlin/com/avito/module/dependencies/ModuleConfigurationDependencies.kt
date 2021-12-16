package com.avito.module.dependencies

import com.avito.module.configurations.ConfigurationType
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency

public fun Project.directDependenciesOnProjects(
    types: Set<ConfigurationType>
): Map<ConfigurationType, Set<Project>> {
    val project = this

    return configurations.groupBy { ConfigurationType.of(it) }
        .filterKeys { configurationType -> types.contains(configurationType) }
        .mapValues { (_, configurations) ->
            configurations
                .flatMap { directProjectDependencies(project, it) }
                .toSet()
        }
}

private fun directProjectDependencies(project: Project, configuration: Configuration): Set<Project> {
    return configuration.dependencies
        .matching { it is ProjectDependency }
        .map { (it as ProjectDependency).dependencyProject }
        // project has dependency to itself in a default configuration
        .filter { it != project }
        .toSet()
}
