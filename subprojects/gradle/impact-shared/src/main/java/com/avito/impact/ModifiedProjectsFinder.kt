package com.avito.impact

import com.avito.impact.configuration.internalModule
import com.avito.impact.fallback.ImpactFallbackDetector
import org.gradle.api.Project

class ModifiedProjectsFinder(
    project: Project,
    private val fallbackDetector: ImpactFallbackDetector
) {
    private val rootProject = project.rootProject

    private val skipAnalysis: Boolean by lazy {
        fallbackDetector.isFallback is ImpactFallbackDetector.Result.Skip
    }

    private val supportedProjects: Set<Project> by lazy {
        rootProject
            .subprojects
            .asSequence()
            .filter { it.isSupportedByImpactAnalysis() }
            .toSet()
    }

    private fun Project.isSupportedByImpactAnalysis(): Boolean =
        with(pluginManager) {
            hasPlugin("com.android.library")
                || hasPlugin("com.android.application")
                || hasPlugin("kotlin")
                || hasPlugin("java")
        }

    fun findModifiedProjects(configurationType: ConfigurationType? = null): Set<ModifiedProject> {
        val reportTypes = if (configurationType == null) {
            ConfigurationType.values()
        } else {
            arrayOf(configurationType)
        }
        return reportTypes
            .flatMap { type ->
                findProjects(type) {
                    it.internalModule.isModified(type)
                }
            }.toSet()
    }

    @Deprecated("Используется только для поиска по ReportType.ANDROID_TESTS. Оптимизация для UI тестов, явно игнорируем изменения в реализации, чтобы не сваливаться всегда в fallback")
    fun findModifiedProjectsWithoutDependencyToAnotherConfigurations(configurationType: ConfigurationType): Set<ModifiedProject> =
        findProjects(configurationType) {
            it.internalModule.getConfiguration(configurationType).let { configuration ->
                configuration.dependencies.any { dependency -> dependency.isModified }
                    || configuration.hasChangedFiles
            }
        }

    private fun findProjects(
        configurationType: ConfigurationType,
        predicate: (project: Project) -> Boolean
    ): Set<ModifiedProject> {
        val projects = if (skipAnalysis) {
            supportedProjects
        } else {
            supportedProjects.filter { predicate(it) }
        }

        return projects
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

    companion object {

        fun from(project: Project): ModifiedProjectsFinder {
            return ModifiedProjectsFinder(project, project.impactFallbackDetector)
        }
    }

}
