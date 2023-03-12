package com.avito.android.plugin.build_metrics.internal.gradle.tasks.slow

import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultListener
import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.android.plugin.build_metrics.internal.module
import com.avito.android.plugin.build_metrics.internal.toTagValue
import com.avito.math.sumByLong
import java.time.Duration

internal class SlowTasksMetricsTracker(
    private val metricsTracker: BuildMetricSender,
    /**
     * We need only the worst ones
     */
    private val topLimit: Int = 100,
    minimumDuration: Duration = Duration.ofSeconds(10),
) : BuildOperationsResultListener {

    private val minimumDurationMs: Long = minimumDuration.toMillis()

    override fun onBuildFinished(result: BuildOperationsResult) {
        val tasks = result.tasksExecutions
        trackCumulativeTime(tasks)
        trackSlowTaskTypes(tasks)
        trackSlowModules(tasks)
        trackSlowTasks(tasks)
    }

    private fun trackCumulativeTime(tasksExecutions: List<TaskExecutionResult>) {
        val cumulativeTimeMs = tasksExecutions.sumByLong { it.elapsedMs }
        val metric = BuildTasksCumulativeMetric(cumulativeTimeMs)
        metricsTracker.send(metric)
    }

    private fun trackSlowTaskTypes(tasks: List<TaskExecutionResult>) {
        val slowTaskTypes = filterSlowMetrics(tasks) { taskResult ->
            SlowMetricType.TaskType(taskResult.type.simpleName)
        }.map { (series, timeMs) ->
            BuildSlowTaskMetric(series, timeMs)
        }
        sendMetrics(slowTaskTypes)
    }

    private fun trackSlowModules(tasks: List<TaskExecutionResult>) {
        val slowModules = filterSlowMetrics(tasks) { taskResult ->
            SlowMetricType.Module(taskResult.path.module.toTagValue())
        }.map { (series, timeMs) ->
            BuildSlowModuleMetric(series, timeMs)
        }
        sendMetrics(slowModules)
    }

    private fun trackSlowTasks(tasks: List<TaskExecutionResult>) {
        val slowModuleTasks = filterSlowMetrics(tasks) { taskResult ->
            SlowMetricType.ModuleTaskType(
                moduleName = taskResult.path.module.toTagValue(),
                taskType = taskResult.type.simpleName
            )
        }.map { (series, timeMs) ->
            BuildSlowModuleTaskMetric(series, timeMs)
        }
        sendMetrics(slowModuleTasks)
    }

    private fun <T : SlowMetricType> filterSlowMetrics(
        tasks: List<TaskExecutionResult>,
        groupBy: (TaskExecutionResult) -> T
    ): List<Pair<T, Long>> {
        return tasks
            .groupBy(groupBy)
            .mapValues { (_, tasks) ->
                tasks.sumByLong { it.elapsedMs }
            }
            .toList()
            .filter { (_, timeMs) ->
                timeMs > minimumDurationMs
            }
            .sortedByDescending { (_, timeMs) ->
                timeMs
            }
            .take(topLimit)
    }

    private fun sendMetrics(metrics: List<BuildMetric>) =
        metrics.forEach { metric -> metricsTracker.send(metric) }
}
