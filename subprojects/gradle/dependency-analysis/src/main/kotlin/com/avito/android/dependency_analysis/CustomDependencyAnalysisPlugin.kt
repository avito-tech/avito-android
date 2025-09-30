package com.avito.android.dependency_analysis

import com.autonomousapps.tasks.ComputeAdviceTask
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register

public class CustomDependencyAnalysisPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (project.isRoot()) {
            configureRoot(project)
        } else {
            configureModule(project)
        }
    }

    private fun configureRoot(project: Project) {
        val extension = project.extensions.create<CustomDependencyAnalysisExtension>(EXTENSION_NAME)

        val dependencyMapProvider = project.provider {
            val catalogs = project.extensions.findByType(VersionCatalogsExtension::class.java)
                ?: return@provider emptyMap()

            catalogs.catalogNames.flatMap { catalogName ->
                val catalog = catalogs.named(catalogName)
                catalog.libraryAliases.map { alias ->
                    catalog.findLibrary(alias).get().get().module.toString() to "${catalog.name}.$alias"
                }
            }.toMap()
        }

        extension.dependencyMap.set(dependencyMapProvider)
    }

    private fun configureModule(project: Project) {
        val extension = project.rootProject.extensions.getByName<CustomDependencyAnalysisExtension>(EXTENSION_NAME)

        project.tasks.register<CheckDependenciesForBreakingAbiTask>("checkDependenciesForBreakingAbi") {
            val pluginId = "com.autonomousapps.dependency-analysis"
            require(project.plugins.hasPlugin(pluginId)) {
                "$pluginId should be applied to ${project.path}"
            }

            val computeAdviceTask = project.tasks.typedNamed<ComputeAdviceTask>("computeAdvice")

            unfilteredProjectAdvice.set(computeAdviceTask.flatMap { it.output })
            exclusions.set(extension.checkDependenciesForBreakingAbiExclusions)
            failureMessage.set(extension.checkDependenciesForBreakingAbiFailureMessage)
            output.set(project.layout.buildDirectory.file("checkDependenciesForBreakingAbiTaskOutput.txt"))
            dependencyMap.set(extension.dependencyMap)
        }
    }

    private companion object {
        const val EXTENSION_NAME = "customDependencyAnalysis"
    }
}
