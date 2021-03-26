package com.avito.module.metrics.metrics

import com.avito.module.configurations.ConfigurationType
import com.avito.module.internal.dependencies.AndroidAppsGraphBuilder
import com.avito.module.internal.dependencies.ProjectWithDeps
import org.gradle.util.Path

internal class CollectAppsMetricsAction(
    private val graphBuilder: AndroidAppsGraphBuilder
) {

    fun collect(): AppsHealthData {
        val appsWithDeps = appsWithDeps()

        return AppsHealthData(
            absolute = absoluteMetrics(appsWithDeps),
            relative = relativeMetrics(appsWithDeps)
        )
    }

    private fun appsWithDeps(): Set<ProjectWithDeps> {
        return graphBuilder
            .buildDependenciesGraph(ConfigurationType.Main)
            .map {
                ProjectWithDeps(it.project, it.allDependencies())
            }
            .toSet()
    }

    private fun absoluteMetrics(appsWithDeps: Set<ProjectWithDeps>): Map<Path, AbsoluteMetrics> {
        return appsWithDeps
            .map { projectWithDeps ->
                val path = Path.path(projectWithDeps.project.path)
                val metrics = AbsoluteMetrics(
                    allDependencies = projectWithDeps.dependencies.size,
                )
                path to metrics
            }
            .toMap()
    }

    private fun relativeMetrics(appsWithDeps: Set<ProjectWithDeps>): Matrix<Path, RelativeMetrics> {
        val metrics = Matrix<Path, RelativeMetrics>()

        for (firstApp in appsWithDeps) {
            for (secondApp in appsWithDeps) {
                if (firstApp.project == secondApp.project) continue

                val row = Path.path(firstApp.project.path)
                val column = Path.path(secondApp.project.path)

                metrics.putIfAbsent(row, column) {
                    computeRelativeMetrics(firstApp, secondApp)
                }
            }
        }
        return metrics
    }

    private fun computeRelativeMetrics(first: ProjectWithDeps, second: ProjectWithDeps): RelativeMetrics {
        return RelativeMetrics(
            baselineDependencies = first.dependencies.map { Path.path(it.path) }.toSet(),
            comparedDependencies = second.dependencies.map { Path.path(it.path) }.toSet()
        )
    }
}
