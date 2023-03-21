package com.avito.android.plugin.build_metrics.tasks

import com.avito.android.critical_path.TaskOperation
import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.StubBuildMetricsSender
import com.avito.android.plugin.build_metrics.internal.gradle.tasks.critical.CriticalPathMetricsTracker
import com.avito.graph.OperationsPath
import com.avito.graphite.series.SeriesName
import com.google.common.truth.Truth.assertThat
import org.gradle.api.Task
import org.gradle.util.Path
import org.junit.jupiter.api.Test
import java.time.Duration

internal class CriticalPathMetricsTrackerTest {

    @Test
    fun `on path ready - sends tasks metrics`() {
        val metrics = processResults(
            taskOperation(
                path = ":lib:build-a",
                type = CustomTask::class.java,
                startMs = 0,
                finishMs = 1_000
            ),
            taskOperation(
                path = ":lib:build-b",
                type = CustomTask::class.java,
                startMs = 0,
                finishMs = 1_000
            ),
            taskOperation(
                path = ":app:build",
                type = CustomTask::class.java,
                startMs = 0,
                finishMs = 3_000
            ),
        )
        assertThat(metrics).contains(
            GraphiteMetric(
                SeriesName.create("gradle.critical.module.task.type", multipart = true)
                    .addTag("module_name", "lib")
                    .addTag("task_type", "CustomTask"),
                "2000"
            )
        )
        assertThat(metrics).contains(
            GraphiteMetric(
                SeriesName.create("gradle.critical.module.task.type", multipart = true)
                    .addTag("module_name", "app")
                    .addTag("task_type", "CustomTask"),
                "3000"
            )
        )
    }

    private fun taskOperation(
        path: String,
        type: Class<out Task>,
        startMs: Long,
        finishMs: Long,
    ) = TaskOperation(
        path = Path.path(path),
        type = type,
        startMs = startMs,
        finishMs = finishMs,
        predecessors = emptySet()
    )

    private fun processResults(vararg tasks: TaskOperation): List<GraphiteMetric> {
        val buildMetricSender = StubBuildMetricsSender()

        val tracker = CriticalPathMetricsTracker(buildMetricSender, Duration.ZERO)
        tracker.onCriticalPathReady(
            OperationsPath(operations = tasks.toList())
        )
        return buildMetricSender.getSentGraphiteMetrics()
    }

    private abstract class CustomTask : Task
}
