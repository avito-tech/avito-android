package com.avito.module.internal.dependencies

import com.avito.module.configurations.ConfigurationType
import org.gradle.api.Project

internal class ProjectConfigurationNode(
    val project: Project,
    val configurationType: ConfigurationType,
    val configurations: Set<ConfigurationNode>
) {

    data class ConfigurationNode(
        val gradleName: String,
        val dependencies: Set<ProjectConfigurationNode>
    )

    fun directDependencies(): Set<ProjectConfigurationNode> {
        return configurations
            .flatMap { it.dependencies }
            .toSet()
    }

    fun allDependencies(): Set<Project> {
        val dependencies = mutableSetOf<Project>()
        // To avoid redundant revisiting the same nodes by different routes
        val visited = mutableSetOf<ProjectConfigurationNode>()
        traverseDependencies(visited) { node: ProjectConfigurationNode ->
            dependencies.add(node.project)
        }
        return dependencies
    }

    private fun ProjectConfigurationNode.traverseDependencies(
        visited: MutableSet<ProjectConfigurationNode>,
        visitor: (ProjectConfigurationNode) -> Unit
    ) {
        configurations
            .flatMap { it.dependencies }
            .forEach { node ->
                if (!visited.contains(node)) {
                    visitor(node)
                    node.traverseDependencies(visited, visitor)
                    visited.add(node)
                }
            }
    }

    override fun equals(other: Any?): Boolean {
        return other is ProjectConfigurationNode && other.project.path == project.path
    }

    override fun hashCode(): Int {
        return project.path.hashCode()
    }

    override fun toString(): String {
        return "ModuleProjectDependenciesNode[${project.path}]"
    }
}
