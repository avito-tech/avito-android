package com.avito.module.dependencies

import com.avito.module.configurations.ConfigurationType
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency

fun Project.dependenciesOnProjects(configurationType: ConfigurationType): Set<DefaultProjectDependency> {
    return configurations
        .filter(configurationType::test)
        .flatMap { configuration ->
            configuration
                .dependencies
                .withType(DefaultProjectDependency::class.java)
        }
        .toSet()
}
