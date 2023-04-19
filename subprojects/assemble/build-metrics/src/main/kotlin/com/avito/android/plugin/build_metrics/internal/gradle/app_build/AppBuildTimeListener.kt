package com.avito.android.plugin.build_metrics.internal.gradle.app_build

import com.android.build.gradle.tasks.PackageApplication
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import com.avito.android.plugin.build_metrics.internal.BuildResultListener
import com.avito.android.plugin.build_metrics.internal.BuildStatus
import com.avito.android.plugin.build_metrics.internal.asSeriesName
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.android.plugin.build_metrics.internal.gradle.app_build.PackageApplicationMetric.ApplicationType
import com.avito.android.plugin.build_metrics.internal.toTagValue
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.withType
import org.gradle.api.Project
import org.gradle.util.Path

internal class AppBuildTimeListener private constructor(
    private val filter: PackageApplicationTasksFilter,
    private val metricTracker: BuildMetricSender
) : BuildResultListener {

    override val name: String = "AppBuildTime"

    override fun onBuildFinished(status: BuildStatus, profile: BuildProfile) {
        val tasks: List<TaskExecution> = profile.getProjects()
            .flatMap { it.getTasks() }
            .filter { filter.isPackageAppTask(it.path) }

        tasks.forEach { task ->
            metricTracker.send(
                createEvent(status, profile, task)
            )
        }
    }

    private fun createEvent(
        status: BuildStatus,
        report: BuildProfile,
        task: TaskExecution,
    ): BuildMetric {
        return PackageApplicationMetric(
            time = task.finish - report.buildStarted,
            status = status.asSeriesName(),
            module = task.module.toTagValue(),
            appType = getPackageTaskAppType(task),
        )
    }

    private fun getPackageTaskAppType(task: TaskExecution): ApplicationType {
        return when {
            task.name.contains("AndroidTest") -> ApplicationType.TEST
            else -> ApplicationType.MAIN
        }
    }

    companion object {

        fun from(project: Project, metricTracker: BuildMetricSender): AppBuildTimeListener {
            return AppBuildTimeListener(
                PackageApplicationTasksFilter(project),
                metricTracker
            )
        }
    }

    private class PackageApplicationTasksFilter(private val project: Project) {

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

        fun isPackageAppTask(path: Path): Boolean {
            return taskPaths.contains(path)
        }
    }
}
