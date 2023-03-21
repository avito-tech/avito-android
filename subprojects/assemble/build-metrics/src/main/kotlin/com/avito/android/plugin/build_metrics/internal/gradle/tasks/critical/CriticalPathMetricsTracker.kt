package com.avito.android.plugin.build_metrics.internal.gradle.tasks.critical

import com.avito.android.critical_path.CriticalPathListener
import com.avito.android.critical_path.TaskOperation
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.android.plugin.build_metrics.internal.module
import com.avito.graph.OperationsPath
import com.avito.math.sumByLong
import java.time.Duration

internal class CriticalPathMetricsTracker(
    private val metricsTracker: BuildMetricSender,
    private val minimalDuration: Duration,
) : CriticalPathListener {

    override fun onCriticalPathReady(path: OperationsPath<TaskOperation>) {
        sendTaskCritical(path)
        sendTaskCriticalCumulative(path)
    }

    private fun sendTaskCriticalCumulative(path: OperationsPath<TaskOperation>) {
        path.operations.groupBy { taskOperation ->
            taskOperation.type.simpleName
        }.forEach { (taskType, tasks) ->
            val duration = Duration.ofMillis(tasks.sumByLong { it.durationMs })
            if (duration > minimalDuration) {
                metricsTracker.send(
                    BuildCriticalPathTaskCumulativeMetric(
                        taskType = taskType,
                        tasksDuration = duration
                    )
                )
            }
        }
    }

    private fun sendTaskCritical(path: OperationsPath<TaskOperation>) {
        path.operations.groupBy { taskOperation ->
            taskOperation.path.module to taskOperation.type.simpleName
        }.forEach { (moduleAndType, tasks) ->
            val (module, taskType) = moduleAndType
            val duration = Duration.ofMillis(tasks.sumByLong { it.durationMs })
            if (duration > minimalDuration) {
                metricsTracker.send(
                    BuildCriticalPathModuleTaskMetric(
                        moduleName = module,
                        taskType = taskType,
                        taskDuration = duration
                    )
                )
            }
        }
    }
}
