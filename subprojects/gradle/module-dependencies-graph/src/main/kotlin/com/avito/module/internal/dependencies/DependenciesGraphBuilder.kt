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
        require(root == root.rootProject) {
            "Project $root must be the root"
        }
    }

    private val logger = loggerFactory.create<DependenciesGraphBuilder>()

    private val visited = mutableMapOf<Project, ModuleProjectDependenciesNode>()

    fun buildDependenciesGraph(): List<ModuleProjectDependenciesNode> {
        root.allprojects.forEach { project ->
            dependenciesOnProjects(project)
        }
        return root.subprojects.map { project ->
            visited.getOrElse(project) {
                throw IllegalStateException("Project $project must be visited")
            }
        }
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
                            val isOtherProject = projectDependency.dependencyProject != project
                            if (!isOtherProject) {
                                logger.debug("Project $project depends on self in configuration ${conf.name}")
                            }
                            isOtherProject
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
