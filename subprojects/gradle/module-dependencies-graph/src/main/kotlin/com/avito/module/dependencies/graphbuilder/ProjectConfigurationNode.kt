package com.avito.module.dependencies.graphbuilder

import com.avito.module.configurations.ConfigurationType
import org.gradle.api.Project

public class ProjectConfigurationNode(
    public val project: Project,
    public val configurationType: ConfigurationType,
    public val configurations: Set<ConfigurationNode>
) {

    public data class ConfigurationNode(
        val gradleName: String,
        val dependencies: Set<ProjectConfigurationNode>
    )

    public fun directDependencies(): Set<ProjectConfigurationNode> {
        return configurations
            .flatMap { it.dependencies }
            .toSet()
    }

    public fun allDependencies(): Set<Project> {
        val dependencies = mutableSetOf<Project>()
        // To avoid redundant revisiting the same nodes by different routes
        val visited = mutableSetOf<ProjectConfigurationNode>()
        traverseDependencies(visited) { node: ProjectConfigurationNode ->
            dependencies.add(node.project)
        }
        return dependencies
    }

    private fun traverseDependencies(
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
