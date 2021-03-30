package com.avito.impact

import com.avito.impact.configuration.internalModule
import com.avito.impact.fallback.ImpactFallbackDetector
import com.avito.module.configurations.ConfigurationType
import org.gradle.api.Project

interface ModifiedProjectsFinder {

    fun allProjects(): Set<Project>

    fun modifiedProjects(): Set<ModifiedProject>

    fun modifiedProjects(configurationType: ConfigurationType): Set<ModifiedProject>

    companion object {

        fun from(project: Project): ModifiedProjectsFinder {
            return ModifiedProjectsFinderImpl(project, project.impactFallbackDetector)
        }
    }
}

internal class ModifiedProjectsFinderImpl(
    project: Project,
    private val fallbackDetector: ImpactFallbackDetector
) : ModifiedProjectsFinder {

    private val rootProject = project.rootProject

    private val skipAnalysis: Boolean by lazy {
        fallbackDetector.isFallback is ImpactFallbackDetector.Result.Skip
    }

    override fun allProjects(): Set<Project> =
        supportedByImpactAnalysisProjects(rootProject)

    override fun modifiedProjects(): Set<ModifiedProject> {
        return ConfigurationType.values()
            .flatMap { modifiedProjects(it) }
            .toSet()
    }

    /**
     * @param configurationType narrow search by configuration type
     *                          null - modified by any configuration
     *
     * @return only modified projects by [configurationType]
     */
    override fun modifiedProjects(configurationType: ConfigurationType): Set<ModifiedProject> {
        return findProjects(rootProject) {
            it.internalModule.isModified(configurationType)
        }
            .map {
                ModifiedProject(
                    project = it,
                    changedFiles = it.internalModule.getConfiguration(configurationType)
                        .changedFiles()
                        .getOrElse { emptyList() }
                )
            }
            .toSet()
    }

    private fun findProjects(
        rootProject: Project,
        predicate: (project: Project) -> Boolean
    ): Set<Project> {
        return if (skipAnalysis) {
            supportedByImpactAnalysisProjects(rootProject)
        } else {
            supportedByImpactAnalysisProjects(rootProject).filter { predicate(it) }.toSet()
        }
    }
}

internal fun supportedByImpactAnalysisProjects(rootProject: Project): Set<Project> {
    return rootProject
        .subprojects
        .asSequence()
        .filter { it.isSupportedByImpactAnalysis() }
        .toSet()
}

internal fun platformModules(rootProject: Project): Set<Project> {
    return rootProject
        .subprojects
        .asSequence()
        .filter { it.isPlatformModule() }
        .toSet()
}

private fun Project.isPlatformModule(): Boolean = pluginManager.hasPlugin("java-platform")

private fun Project.isSupportedByImpactAnalysis(): Boolean =
    with(pluginManager) {
        hasPlugin("com.android.library")
            || hasPlugin("com.android.application")
            || hasPlugin("kotlin")
            || hasPlugin("java")
    }
