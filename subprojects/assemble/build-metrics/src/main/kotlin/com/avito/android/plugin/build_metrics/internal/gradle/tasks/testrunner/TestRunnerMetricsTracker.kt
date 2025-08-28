package com.avito.android.plugin.build_metrics.internal.gradle.tasks.testrunner

import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultListener
import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.android.plugin.build_metrics.internal.module
import com.avito.android.plugin.build_metrics.internal.toTagValue
import com.avito.math.sumByLong
import org.gradle.api.Task

internal class TestRunnerMetricsTracker(
    private val metricsTracker: BuildMetricSender,
) : BuildOperationsResultListener {

    override val name: String = "TestRunnerMetrics"

    override fun onBuildFinished(result: BuildOperationsResult) {
        val tasks = result.tasksExecutions
        trackTestRunnerExecutionTime(tasks)
    }

    private fun trackTestRunnerExecutionTime(tasksExecutions: List<TaskExecutionResult>) {
        val testRunnerTasks = tasksExecutions.filter { task ->
            isTestRunnerTask(task.type)
        }

        val groupedByModuleAndTags = testRunnerTasks.groupBy { task ->
            val moduleName = task.path.module.toTagValue()
            moduleName to task.tags
        }

        groupedByModuleAndTags.forEach { (key, tasks) ->
            val (moduleName, tags) = key
            val totalTimeMs = tasks.sumByLong { it.elapsedMs }
            if (totalTimeMs > 0) {
                val metric = TestRunnerExecutionTimeMetric(
                    moduleName = moduleName,
                    tags = tags,
                    timeMs = totalTimeMs
                )
                metricsTracker.send(metric)
            }
        }
    }

    private fun isTestRunnerTask(taskClass: Class<out Task>): Boolean {
        return INSTRUMENTATION_TASKS.contains(taskClass.simpleName)
    }

    companion object {
        private val INSTRUMENTATION_TASKS = listOf(
            "InstrumentationTestsTask",
            "AvitoEmceeTestTask"
        )
    }
}
