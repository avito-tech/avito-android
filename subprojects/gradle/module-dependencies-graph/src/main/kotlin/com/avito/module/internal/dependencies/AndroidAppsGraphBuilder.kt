package com.avito.module.internal.dependencies

import com.avito.module.configurations.ConfigurationType
import com.avito.module.internal.configurations.ConfigurationCoordinate
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

    fun buildDependenciesGraph(): Set<ModuleProjectDependenciesNode> {
        val appsProjects = mutableListOf<Project>()

        root.allprojects.forEach { project ->
            if (project.plugins.hasPlugin("com.android.application")) {
                appsProjects.add(project)
            }
        }
        val appsNodes = mutableSetOf<ModuleProjectDependenciesNode>()

        graphBuilder.buildDependenciesGraph().forEach { node ->
            traverseGraph(node) { visitedNode ->
                if (appsProjects.contains(visitedNode.project)) {
                    appsNodes.add(visitedNode)
                }
            }
        }
        return appsNodes
    }

    fun buildDependenciesGraphFlatten(type: ConfigurationType): List<Pair<Project, Set<Project>>> {
        return buildDependenciesGraph()
            .map { rootNode ->
                rootNode.project to rootNode.dependencies
                    .filterKeys { it.type == type }
                    .flatDependencies(ConfigurationType.Main)
            }
    }

    private fun Map<ConfigurationCoordinate, Set<ModuleProjectDependenciesNode>>.flatDependencies(
        type: ConfigurationType
    ): Set<Project> {
        return mapValues { (_, nodeSet) ->
            nodeSet.flatMap { node ->
                mutableSetOf(node.project).also {
                    it.addAll(
                        node.dependencies
                            .filterKeys { it.type == type }
                            .flatDependencies(type)
                    )
                }
            }
        }.values.flatten().toSet()
    }

    private fun traverseGraph(
        node: ModuleProjectDependenciesNode,
        visit: (ModuleProjectDependenciesNode) -> Unit
    ) {
        visit(node)
        node.dependencies.values.forEach { deps ->
            deps.forEach {
                traverseGraph(it, visit)
            }
        }
    }
}
