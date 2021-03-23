package com.avito.module.internal.dependencies

import com.avito.module.configurations.ConfigurationType
import org.gradle.api.Project

internal class ModuleProjectConfigurationDependenciesNode(
    val project: Project,
    val configurationType: ConfigurationType,
    val dependencies: Map<String, Set<ModuleProjectConfigurationDependenciesNode>>
) {

    fun directDependencies(): Set<ModuleProjectConfigurationDependenciesNode> {
        return dependencies
            .values
            .flatten()
            .toSet()
    }

    fun allDependencies(): Set<Project> {
        val dependencies = mutableSetOf<Project>()
        // To avoid redundant revisiting the same nodes by different routes
        val visited = mutableSetOf<ModuleProjectConfigurationDependenciesNode>()
        traverseDependencies(visited) { node: ModuleProjectConfigurationDependenciesNode ->
            dependencies.add(node.project)
        }
        return dependencies
    }

    private fun ModuleProjectConfigurationDependenciesNode.traverseDependencies(
        visited: MutableSet<ModuleProjectConfigurationDependenciesNode>,
        visitor: (ModuleProjectConfigurationDependenciesNode) -> Unit
    ) {
        dependencies.values.flatten().forEach { node ->
            if (!visited.contains(node)) {
                visitor(node)
                node.traverseDependencies(visited, visitor)
                visited.add(node)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is ModuleProjectConfigurationDependenciesNode && other.project.path == project.path
    }

    override fun hashCode(): Int {
        return project.path.hashCode()
    }

    override fun toString(): String {
        return "ModuleProjectDependenciesNode[${project.path}]"
    }
}
