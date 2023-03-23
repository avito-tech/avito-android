package com.avito.module.dependencies.graphbuilder

import com.avito.module.configurations.ConfigurationType

public class AndroidAppsGraphBuilder(
    private val graphBuilder: DependenciesGraphBuilder
) {

    public fun buildDependenciesGraph(type: ConfigurationType): Set<ProjectConfigurationNode> {
        return graphBuilder.buildDependenciesGraph(type)
            .filter { node ->
                node.project.plugins.hasPlugin("com.android.application")
            }.toSet()
    }
}
