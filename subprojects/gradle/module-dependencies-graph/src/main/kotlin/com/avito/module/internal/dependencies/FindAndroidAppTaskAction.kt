package com.avito.module.internal.dependencies

import com.avito.module.configurations.ConfigurationType
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.MultipleSuitableApps
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.NoSuitableApps
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.OneSuitableApp
import org.gradle.util.Path

internal class FindAndroidAppTaskAction(
    private val graphBuilder: AndroidAppsGraphBuilder
) {

    fun findAppFor(modules: List<Path>, configurations: Set<ConfigurationType>): Verdict {
        val suitableApps = configurations
            .map {
                graphBuilder.buildDependenciesGraphFlatten(it)
            }
            .flatten()
            .groupBy { it.project }
            .mapValues { it.value.flatMap { it.dependencies }.toSet() }
            .filter { (_, dependencyProjects) ->
                dependencyProjects
                    .map { Path.path(it.path) }
                    .toSet()
                    .containsAll(modules)
            }
            .map {
                ProjectWithDeps(it.key, it.value)
            }

        return when {
            suitableApps.isEmpty() -> NoSuitableApps
            suitableApps.size == 1 -> OneSuitableApp(suitableApps.first())
            else -> MultipleSuitableApps(suitableApps.toSet())
        }
    }

    sealed class Verdict {
        data class OneSuitableApp(val projectWithDeps: ProjectWithDeps) : Verdict()
        data class MultipleSuitableApps(val projectsWithDeps: Set<ProjectWithDeps>) : Verdict()
        object NoSuitableApps : Verdict()
    }
}
