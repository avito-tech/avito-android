package com.avito.android.plugin.build_metrics.internal.tasks

import com.avito.android.build_metrics.BuildMetricTracker
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultListener
import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.module
import com.avito.android.plugin.build_metrics.internal.toSeriesName
import com.avito.android.stats.SeriesName
import com.avito.android.stats.TimeMetric
import com.avito.math.sumByLong

internal class SlowTasksMetricsTracker(
    private val metricsTracker: BuildMetricTracker,
) : BuildOperationsResultListener {

    override fun onBuildFinished(result: BuildOperationsResult) {
        val tasks = result.tasksExecutions
        trackCumulativeTime(tasks)
        trackSlowTaskTypes(tasks)
        trackSlowModules(tasks)
        trackSlowTasks(tasks)
    }

    private fun trackCumulativeTime(tasksExecutions: List<TaskExecutionResult>) {
        val timeMs = tasksExecutions.sumByLong { it.elapsedMs }
        val name = SeriesName.create("tasks", "cumulative", "any")
        metricsTracker.track(
            TimeMetric(name, timeMs)
        )
    }

    private fun trackSlowTaskTypes(tasks: List<TaskExecutionResult>) {
        trackCumulativeTimeByAttribute(tasks) { taskResult ->
            SeriesName.create("type")
                .append(taskResult.type.simpleName)
        }
    }

    private fun trackSlowModules(tasks: List<TaskExecutionResult>) {
        trackCumulativeTimeByAttribute(tasks) { taskResult ->
            SeriesName.create("module")
                .append(taskResult.path.module.toSeriesName())
        }
    }

    private fun trackSlowTasks(tasks: List<TaskExecutionResult>) {
        trackCumulativeTimeByAttribute(tasks) { taskResult ->
            SeriesName.create("task")
                .append(taskResult.path.module.toSeriesName())
                .append(taskResult.type.simpleName)
        }
    }

    private fun trackCumulativeTimeByAttribute(
        tasks: List<TaskExecutionResult>,
        groupBy: (TaskExecutionResult) -> SeriesName
    ) {
        tasks
            .groupBy(groupBy)
            .mapValues { (_, tasks) ->
                tasks.sumByLong { it.elapsedMs }
            }
            .toList()
            .filter { (_, timeMs) ->
                timeMs > considerableTimeMs
            }
            .sortedByDescending { (_, timeMs) ->
                timeMs
            }
            .take(TOP_LIMIT)
            .forEach { (groupName, timeMs) ->
                val name = SeriesName.create("tasks", "slow").append(groupName)
                metricsTracker.track(
                    TimeMetric(name, timeMs)
                )
            }
    }
}

/**
 * We need only the worst ones
 */
private const val TOP_LIMIT = 100

private const val considerableTimeMs = 100
