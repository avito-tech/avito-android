package com.avito.android.plugin.build_metrics.internal.gradle.app_build

import com.android.build.gradle.tasks.PackageApplication
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultListener
import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.asSeriesName
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.android.plugin.build_metrics.internal.gradle.app_build.clickstream.AppBuildTimeMetric
import com.avito.android.plugin.build_metrics.internal.gradle.app_build.graphite.PackageApplicationMetric
import com.avito.android.plugin.build_metrics.internal.module
import com.avito.android.plugin.build_metrics.internal.toTagValue
import java.time.Duration
import java.time.Instant

internal class AppBuildTimeListener(
    private val sender: BuildMetricSender,
    private val userName: String,
) : BuildOperationsResultListener {

    override val name: String = "AppBuildTime"

    override fun onBuildFinished(result: BuildOperationsResult) {
        val packageAppTasks = result
            .tasksExecutions
            .filter { it.type == PackageApplication::class.java }

        packageAppTasks.forEach { task ->
            val duration = Duration
                .between(result.buildResult.startTime, Instant.ofEpochMilli(task.endMs))
                .toMillis()

            sender.send(
                PackageApplicationMetric(
                    duration = duration,
                    status = result.buildResult.status.asSeriesName(),
                    module = task.path.module.toTagValue(),
                    appType = getPackageTaskAppType(task),
                )
            )

            sender.send(
                AppBuildTimeMetric(
                    duration = duration,
                    status = result.buildResult.status.asSeriesName(),
                    appName = task.path.module.toTagValue(),
                    appType = getPackageTaskAppType(task),
                    userName = userName,
                )
            )
        }
    }

    private fun getPackageTaskAppType(task: TaskExecutionResult): ApplicationType {
        return when {
            task.name.contains("AndroidTest") -> ApplicationType.TEST
            else -> ApplicationType.MAIN
        }
    }
}
