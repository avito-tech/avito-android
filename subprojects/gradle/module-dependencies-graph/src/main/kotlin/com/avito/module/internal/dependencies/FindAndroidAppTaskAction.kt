package com.avito.module.internal.dependencies

import com.avito.module.configurations.ConfigurationType
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.MultipleSuitableApps
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.NoSuitableApps
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.OneSuitableApp
import org.gradle.api.Project
import org.gradle.util.Path

internal class FindAndroidAppTaskAction(
    private val graphBuilder: AndroidAppsGraphBuilder
) {

    fun findAppFor(modules: Set<Path>, configurations: Set<ConfigurationType>): Verdict {
        val apps = configurations
            .map {
                graphBuilder.buildDependenciesGraphFlatten(it)
            }
            .flatten()
            .groupBy { it.project }
            .mapValues { it.value.flatMap { it.dependencies }.toSet() }
            .map { (project, deps) -> ProjectWithDeps(project, deps) }
            .toSet()

        val suitableApps = apps
            .filter { (_, dependencyProjects) ->
                dependencyProjects
                    .map { Path.path(it.path) }
                    .toSet()
                    .containsAll(modules)
            }

        return when {
            suitableApps.isEmpty() -> noSuitableApps(modules, apps)
            suitableApps.size == 1 -> OneSuitableApp(suitableApps.first())
            else -> MultipleSuitableApps(suitableApps.toSet())
        }
    }

    private fun noSuitableApps(
        modules: Set<Path>,
        projects: Set<ProjectWithDeps>
    ): NoSuitableApps {
        val result = projects
            .map { (project, dependencies) ->
                project to dependencies.map { Path.path(it.path) }.toSet()
            }
            .map { (project, dependencies) ->
                val presented = dependencies.intersect(modules)
                NoSuitableApps.Result(
                    project = project,
                    dependencies = dependencies,
                    presentedModules = presented,
                    missedModules = modules - presented
                )
            }
            .toSet()
        return NoSuitableApps(result)
    }

    sealed class Verdict {
        data class OneSuitableApp(val projectWithDeps: ProjectWithDeps) : Verdict()
        data class MultipleSuitableApps(val projectsWithDeps: Set<ProjectWithDeps>) : Verdict() {
            init {
                require(projectsWithDeps.size > 1) {
                    "MultipleSuitableApps must contain more then one app"
                }
            }
        }

        data class NoSuitableApps(val result: Set<Result>) : Verdict() {

            data class Result(
                val project: Project,
                val dependencies: Set<Path>,
                val presentedModules: Set<Path>,
                val missedModules: Set<Path>
            ) {
                init {
                    require(missedModules.isNotEmpty()) {
                        "When verdict is no suitable app than Result must contain missed modules"
                    }
                }
            }
        }
    }
}
