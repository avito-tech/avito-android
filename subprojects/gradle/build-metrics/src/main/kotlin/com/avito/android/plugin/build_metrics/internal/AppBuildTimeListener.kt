package com.avito.android.plugin.build_metrics.internal

import com.android.build.gradle.tasks.PackageApplication
import com.avito.android.build_metrics.BuildMetricTracker
import com.avito.android.build_metrics.BuildStatus
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsMetric
import com.avito.android.stats.TimeMetric
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.withType
import org.gradle.api.Project
import org.gradle.util.Path

internal class AppBuildTimeListener(
    private val filter: TasksFilter,
    private val metricTracker: BuildMetricTracker
) : BuildResultListener {

    interface TasksFilter {

        fun isTracked(path: Path): Boolean
    }

    override fun onBuildFinished(status: BuildStatus, profile: BuildProfile) {
        val tasks: List<TaskExecution> = profile.getProjects()
            .flatMap { it.getTasks() }
            .filter { filter.isTracked(it.path) }

        tasks.forEach { task ->
            metricTracker.track(
                status,
                createEvent(profile, task)
            )
        }
    }

    private fun createEvent(report: BuildProfile, task: TaskExecution): StatsMetric {
        val eventName = SeriesName.create("app-build")
            .append(task.module.toSeriesName())
            .append(task.name)
            .append("finish")

        val time = task.finish - report.buildStarted

        return TimeMetric(eventName, time)
    }

    companion object {

        fun from(project: Project, metricTracker: BuildMetricTracker): AppBuildTimeListener {
            return AppBuildTimeListener(
                AppBuildTasksFilter(project),
                metricTracker
            )
        }
    }

    private class AppBuildTasksFilter(private val project: Project) : TasksFilter {

        private val taskPaths = mutableSetOf<Path>()

        init {
            check(project.isRoot()) { "Project ${project.path} must be root" }

            project.subprojects.forEach { module ->
                module.pluginManager.withPlugin("com.android.application") {
                    // package tasks are the last before consuming APK
                    // assemble task is an optional "marker"
                    module.tasks.withType<PackageApplication>().configureEach { task ->
                        taskPaths.add(Path.path(task.path))
                    }
                }
            }
        }

        override fun isTracked(path: Path): Boolean {
            return taskPaths.contains(path)
        }
    }
}
