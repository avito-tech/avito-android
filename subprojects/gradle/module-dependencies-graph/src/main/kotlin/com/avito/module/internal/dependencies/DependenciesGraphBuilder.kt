package com.avito.module.internal.dependencies

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.module.configurations.ConfigurationType
import com.avito.module.internal.configurations.ConfigurationCoordinate
import com.avito.module.internal.dependencies.ProjectConfigurationNode.ConfigurationNode
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

    private val visited = mutableMapOf<ProjectConfigurationCoordinate, ProjectConfigurationNode>()

    fun buildDependenciesGraph(configurationType: ConfigurationType): Set<ProjectConfigurationNode> {
        root.allprojects.forEach { project ->
            dependenciesOnProjects(project, configurationType)
        }
        return visited.filterKeys { it.configurationType == configurationType }
            .values
            .toSet()
    }

    private fun dependenciesOnProjects(
        project: Project,
        configurationType: ConfigurationType
    ): ProjectConfigurationNode {
        return visited.getOrPut(ProjectConfigurationCoordinate(project, configurationType)) {
            ProjectConfigurationNode(
                project = project,
                configurationType = configurationType,
                configurations = project.configurations
                    .asSequence()
                    .map { conf ->
                        ConfigurationCoordinate(conf.name, ConfigurationType.of(conf)) to conf
                    }
                    .filter { (coordinate, _) ->
                        coordinate.type == configurationType
                    }
                    .map { (coordinate, conf) ->
                        coordinate.gradleName to conf.dependencies
                            .withType(DefaultProjectDependency::class.java)
                            .filter { projectDependency ->
                                val isOtherProject = projectDependency.dependencyProject != project
                                if (!isOtherProject) {
                                    logger.debug("Project $project depends on self in configuration ${conf.name}")
                                }
                                isOtherProject
                            }
                            .map { projectDependency ->
                                dependenciesOnProjects(projectDependency.dependencyProject, ConfigurationType.Main)
                            }
                            .toSet()
                    }
                    .map { (configurationName, dependencies) ->
                        ConfigurationNode(configurationName, dependencies)
                    }
                    .toSet()
            )
        }
    }

    private data class ProjectConfigurationCoordinate(
        val project: Project,
        val configurationType: ConfigurationType
    )
}
