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

    fun findModifiedProjects(reportType: ReportType? = null): Set<ModifiedProject> {
        val reportTypes = if (reportType == null) {
            ReportType.values()
        } else {
            arrayOf(reportType)
        }
        return reportTypes
            .flatMap { type ->
                findProjects(type) {
                    it.internalModule.isModified(type)
                }
            }.toSet()
    }

    @Deprecated("Используется только для поиска по ReportType.ANDROID_TESTS. Оптимизация для UI тестов, явно игнорируем изменения в реализации, чтобы не сваливаться всегда в fallback")
    fun findModifiedProjectsWithoutDependencyToAnotherConfigurations(reportType: ReportType): Set<ModifiedProject> =
        findProjects(reportType) {
            it.internalModule.getConfiguration(reportType).let { configuration ->
                configuration.dependencies.any { dependency -> dependency.isModified }
                    || configuration.hasChangedFiles
            }
        }

    private fun findProjects(
        reportType: ReportType,
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
                    changedFiles = it.internalModule.getConfiguration(reportType)
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
