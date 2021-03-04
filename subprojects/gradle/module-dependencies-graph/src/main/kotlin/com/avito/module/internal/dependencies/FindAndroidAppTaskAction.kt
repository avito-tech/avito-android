package com.avito.module.internal.dependencies

import com.avito.module.configurations.ConfigurationType
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.MultipleSuitableApps
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.NoSuitableApps
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.OneSuitableApp
import org.gradle.api.Project
import org.gradle.util.Path

internal class FindAndroidAppTaskAction(
    private val graphBuilder: DependenciesGraphBuilder
) {

    fun findAppFor(modules: List<Path>, configuration: ConfigurationType): Verdict {
        val suitableApps = graphBuilder
            .buildDependenciesGraphFlatten(configuration)
            .filter { (_, dependencyProjects) ->
                dependencyProjects
                    .map { Path.path(it.path) }
                    .toSet()
                    .containsAll(modules)
            }
        return when {
            suitableApps.isEmpty() -> NoSuitableApps
            suitableApps.size == 1 -> OneSuitableApp(suitableApps.first())
            else -> MultipleSuitableApps(suitableApps.toSet())
        }
    }

    sealed class Verdict {
        data class OneSuitableApp(val project: Pair<Project, Set<Project>>) : Verdict()
        data class MultipleSuitableApps(val projects: Set<Pair<Project, Set<Project>>>) : Verdict()
        object NoSuitableApps : Verdict()
    }
}
