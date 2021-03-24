package com.avito.module.internal.dependencies

import com.avito.module.configurations.ConfigurationType

internal class AndroidAppsGraphBuilder(
    private val graphBuilder: DependenciesGraphBuilder
) {

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
