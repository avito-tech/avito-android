package com.avito.module.dependencies.graphbuilder

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph

public class SimpleModuleGraph {

    public fun compute(root: Project): SimpleDirectedGraph<Project, DefaultEdge> {
        val result = SimpleDirectedGraph<Project, DefaultEdge>(DefaultEdge::class.java)

        root.subprojects.forEach { project ->
            if (project.isPlainDir()) {
                return@forEach
            }
            result.addVertex(project)

            project.configurations.map { configuration ->
                configuration.dependencies
                    .withType(ProjectDependency::class.java)
                    .forEach { dependency ->
                        val dependencyProject = dependency.dependencyProject

                        if (project != dependencyProject) {
                            result.addVertex(dependencyProject)
                            result.addEdge(project, dependencyProject)
                        }
                    }
            }
        }

        return result
    }

    private fun Project.isPlainDir(): Boolean {
        return !file("build.gradle").exists() && !file("build.gradle.kts").exists()
    }
}
