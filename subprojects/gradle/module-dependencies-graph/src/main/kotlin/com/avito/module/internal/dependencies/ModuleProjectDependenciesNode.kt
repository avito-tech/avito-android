package com.avito.module.internal.dependencies

import com.avito.module.internal.configurations.ConfigurationCoordinate
import org.gradle.api.Project

internal class ModuleProjectDependenciesNode(
    val project: Project,
    val dependencies: Map<ConfigurationCoordinate, Set<ModuleProjectDependenciesNode>>
) {
    override fun equals(other: Any?): Boolean {
        return other is ModuleProjectDependenciesNode && other.project.path == project.path
    }

    override fun hashCode(): Int {
        return project.path.hashCode()
    }

    override fun toString(): String {
        return "ModuleProjectDependenciesNode[${project.path}]"
    }
}
