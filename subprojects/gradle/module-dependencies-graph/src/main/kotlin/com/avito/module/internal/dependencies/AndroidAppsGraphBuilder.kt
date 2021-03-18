package com.avito.module.internal.dependencies

import com.avito.module.configurations.ConfigurationType
import org.gradle.api.Project

internal class AndroidAppsGraphBuilder(
    private val root: Project,
    private val graphBuilder: DependenciesGraphBuilder
) {

    init {
        require(root == root.rootProject) {
            "Project $root must be the root"
        }
    }

    fun buildDependenciesGraph(type: ConfigurationType): Set<ModuleProjectConfigurationDependenciesNode> {
        return graphBuilder.buildDependenciesGraph(type)
            .filter { node ->
                node.project.plugins.hasPlugin("com.android.application")
            }.toSet()
    }

    fun buildDependenciesGraphFlatten(type: ConfigurationType): List<ProjectWithDeps> {
        return buildDependenciesGraph(type)
            .map { rootNode ->
                ProjectWithDeps(rootNode.project, rootNode.allDependencies())
            }
    }
}
