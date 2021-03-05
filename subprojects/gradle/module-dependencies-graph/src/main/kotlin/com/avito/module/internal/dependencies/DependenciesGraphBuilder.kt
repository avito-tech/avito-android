package com.avito.module.internal.dependencies

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.module.configurations.ConfigurationType
import com.avito.module.internal.configurations.ConfigurationCoordinate
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency

internal class DependenciesGraphBuilder(
    private val root: Project,
    loggerFactory: LoggerFactory
) {

    init {
        require(root == root.rootProject)
    }

    private val logger = loggerFactory.create<DependenciesGraphBuilder>()
    private val rootProjects = mutableListOf<Project>()
    private val visited = mutableMapOf<Project, ModuleProjectDependenciesNode>()

    fun buildDependenciesGraph(): Set<ModuleProjectDependenciesNode> {
        root.allprojects.forEach { project ->
            if (project.plugins.hasPlugin("com.android.application")) {
                rootProjects.add(project)
            }
            dependenciesOnProjects(project)
        }
        return rootProjects.map {
            visited.getOrElse(it) {
                throw IllegalStateException("Project $it must be visited")
            }
        }.toSet()
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

    private fun dependenciesOnProjects(project: Project): ModuleProjectDependenciesNode {
        return visited.getOrPut(project) {
            ModuleProjectDependenciesNode(
                project = project,
                dependencies = project.configurations.map { conf ->
                    val coordinate = ConfigurationCoordinate(conf.name, ConfigurationType.of(conf))
                    coordinate to conf.dependencies
                        .withType(DefaultProjectDependency::class.java)
                        .filter { projectDependency ->
                            val result = projectDependency.dependencyProject != project
                            if (!result) {
                                logger.debug("Project $project depends on self in configuration ${conf.name}")
                            }
                            result
                        }
                        .map { projectDependency ->
                            dependenciesOnProjects(projectDependency.dependencyProject)
                        }
                        .toSet()
                }.toMap()
            )
        }
    }
}
