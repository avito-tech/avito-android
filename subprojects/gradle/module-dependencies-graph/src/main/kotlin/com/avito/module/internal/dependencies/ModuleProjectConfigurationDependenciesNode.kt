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
        return dependencies.mapValues { (_, nodeSet) ->
            nodeSet.flatMap { node ->
                mutableSetOf(node.project).also {
                    it.addAll(
                        node.allDependencies()
                    )
                }
            }
        }.values.flatten().toSet()
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
