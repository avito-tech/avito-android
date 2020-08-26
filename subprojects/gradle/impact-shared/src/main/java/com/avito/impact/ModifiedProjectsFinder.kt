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

    fun findModifiedProjects(reportType: ReportType? = null): Set<ModifiedProject> {
        val reportTypes = if (reportType == null) {
            ReportType.values()
        } else {
            arrayOf(reportType)
        }
        return reportTypes
            .flatMap { type ->
                findProjects(rootProject, type) {
                    it.internalModule.isModified(type)
                }
            }.toSet()
    }

    @Deprecated("Используется только для поиска по ReportType.ANDROID_TESTS. Оптимизация для UI тестов, явно игнорируем изменения в реализации, чтобы не сваливаться всегда в fallback")
    fun findModifiedProjectsWithoutDependencyToAnotherConfigurations(reportType: ReportType): Set<ModifiedProject> =
        findProjects(rootProject, reportType) {
            it.internalModule.getConfiguration(reportType).let { configuration ->
                configuration.dependencies.any { dependency -> dependency.isModified }
                    || configuration.hasChangedFiles
            }
        }

    private fun findProjects(
        rootProject: Project,
        reportType: ReportType,
        predicate: (project: Project) -> Boolean
    ): Set<ModifiedProject> {
        val projects = if (skipAnalysis) {
            supportedByImpactAnalysisProjects(rootProject)
        } else {
            supportedByImpactAnalysisProjects(rootProject).filter { predicate(it) }
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
