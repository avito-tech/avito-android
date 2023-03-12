package com.avito.android.plugin.build_metrics.internal.gradle.tasks.critical

import com.avito.android.critical_path.CriticalPathListener
import com.avito.android.critical_path.TaskOperation
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.android.plugin.build_metrics.internal.module
import com.avito.graph.OperationsPath
import com.avito.math.sumByLong
import java.time.Duration

internal class CriticalPathMetricsTracker(
    private val metricsTracker: BuildMetricSender
) : CriticalPathListener {

    override fun onCriticalPathReady(path: OperationsPath<TaskOperation>) {
        sendTaskCritical(path)
        sendTaskCriticalCumulative(path)
    }

    private fun sendTaskCriticalCumulative(path: OperationsPath<TaskOperation>) {
        path.operations.groupBy { taskOperation ->
            taskOperation.type.simpleName
        }.forEach { (taskType, tasks) ->
            val durationMs = tasks.sumByLong { it.durationMs }
            metricsTracker.send(
                BuildCriticalPathTaskCumulativeMetric(
                    taskType = taskType,
                    tasksDuration = Duration.ofMillis(durationMs)
                )
            )
        }
    }

    private fun sendTaskCritical(path: OperationsPath<TaskOperation>) {
        path.operations.groupBy { taskOperation ->
            taskOperation.path.module to taskOperation.type.simpleName
        }.forEach { (moduleAndType, tasks) ->
            val durationMs = tasks.sumByLong { it.durationMs }
            val (module, taskType) = moduleAndType
            metricsTracker.send(
                BuildCriticalPathModuleTaskMetric(
                    moduleName = module,
                    taskType = taskType,
                    taskDuration = Duration.ofMillis(durationMs)
                )
            )
        }
    }
}
